from __future__ import annotations

import csv
import json
import re
from collections import Counter
from pathlib import Path

ROOT = Path(__file__).resolve().parents[1]
WORDS_JSON = ROOT / "app" / "src" / "main" / "assets" / "words.json"
OVERRIDES_JSON = ROOT / "app" / "src" / "main" / "assets" / "phonics_overrides.json"
OUT_CSV = ROOT / "词汇" / "单词" / "00_自然拼读质量问题清单.csv"
OUT_MD = ROOT / "词汇" / "单词" / "00_自然拼读质量报告.md"

HIGH_RISK_PATTERNS = [
    "tion",
    "sion",
    "cian",
    "ture",
    "sure",
    "ough",
    "igh",
    "air",
    "ear",
    "eer",
    "ire",
    "are",
    "sh",
    "ch",
    "th",
    "ph",
    "ck",
    "ng",
    "qu",
    "kn",
    "wr",
    "mb",
    "ui",
    "ee",
    "ea",
    "ai",
    "ay",
    "oa",
    "ow",
    "ou",
    "oo",
    "oi",
    "oy",
]

STRICT_SAMPLES = {
    "box": ["b", "o", "x"],
    "require": ["r", "e", "qu", "ire"],
    "achievement": ["a", "ch", "ie", "v", "e", "ment"],
    "agreement": ["a", "gree", "ment"],
    "thought": ["th", "ough", "t"],
    "fruit": ["fr", "ui", "t"],
    "shepherd": ["sh", "e", "p", "h", "er", "d"],
    "interested": ["in", "ter", "est", "ed"],
    "cleaned": ["clean", "ed"],
    "magazine": ["m", "a", "g", "a", "zine"],
}


def word_key(value: str) -> str:
    return re.sub(r"[^a-z]", "", value.lower())


def normalize_ipa(value: str) -> str:
    value = value.strip()
    if value.startswith("/") and value.endswith("/"):
        value = value[1:-1]
    return (
        value.replace("ɡ", "g")
        .replace("ˈ", "")
        .replace("ˌ", "")
        .replace(".", "")
        .replace(" ", "")
        .replace("/", "")
    )


def add_issue(issues: list[dict], item: dict, level: str, rule: str, detail: str) -> None:
    issues.append(
        {
            "级别": level,
            "规则": rule,
            "单词": item.get("word", ""),
            "音标": item.get("phonetic", ""),
            "分块": " | ".join(item.get("blocks", [])),
            "分块音标": " ".join(item.get("sounds", [])),
            "类型": " ".join(item.get("types", [])),
            "说明": detail,
        }
    )


def contains_block(blocks: list[str], pattern: str) -> bool:
    return pattern in [block.lower() for block in blocks]


def block_covers_pattern(blocks: list[str], pattern: str) -> bool:
    return any(pattern in block.lower() for block in blocks)


def pattern_is_split(word: str, blocks: list[str], pattern: str) -> bool:
    lowered = word.lower()
    if pattern not in lowered:
        return False
    if block_covers_pattern(blocks, pattern):
        return False
    return True


def has_long_letter_run(blocks: list[str]) -> bool:
    run = 0
    for block in blocks:
        if len(word_key(block)) == 1:
            run += 1
            if run >= 5:
                return True
        else:
            run = 0
    return False


def main() -> int:
    words = json.loads(WORDS_JSON.read_text(encoding="utf-8"))
    overrides = json.loads(OVERRIDES_JSON.read_text(encoding="utf-8")) if OVERRIDES_JSON.exists() else {}
    issues: list[dict] = []

    by_word = {item["word"].lower(): item for item in words}
    for item in words:
        word = item["word"]
        lowered = word.lower()
        blocks = item.get("blocks", [])
        sounds = item.get("sounds", [])
        types = item.get("types", [])
        joined_blocks = word_key("".join(blocks))

        if joined_blocks != word_key(word):
            add_issue(issues, item, "ERROR", "blocks_join_word", "分块拼回单词后与原单词不一致。")

        if len(blocks) != len(types):
            add_issue(issues, item, "ERROR", "blocks_types_count", "分块数量和类型数量不一致。")

        if sounds and len(blocks) != len(sounds):
            add_issue(issues, item, "ERROR", "blocks_sounds_count", "分块数量和分块音标数量不一致。")

        if sounds:
            merged_sound = "".join(normalize_ipa(sound) for sound in sounds)
            if merged_sound != normalize_ipa(item.get("phonetic", "")):
                add_issue(issues, item, "ERROR", "sounds_join_phonetic", "分块音标拼回后与整词音标不一致。")

        for pattern in HIGH_RISK_PATTERNS:
            if pattern_is_split(lowered, blocks, pattern):
                add_issue(issues, item, "WARN", "high_risk_pattern_split", f"高风险拼读组合 `{pattern}` 没有作为整体分块。")

        if not sounds and any(pattern in lowered for pattern in HIGH_RISK_PATTERNS):
            add_issue(issues, item, "WARN", "risky_word_without_sounds", "包含高风险拼读组合，但没有可靠分块音标。")

        if not sounds and has_long_letter_run(blocks):
            add_issue(issues, item, "WARN", "weak_visual_split", "连续单字母视觉拆分过长，教学价值低。")

        if lowered.endswith("ed") and len(lowered) > 3 and normalize_ipa(item.get("phonetic", "")).endswith(("d", "t", "ɪd")):
            if not contains_block(blocks, "ed"):
                add_issue(issues, item, "WARN", "ed_not_grouped", "`-ed` 词尾没有作为整体分块。")

        if lowered.endswith("ing") and len(lowered) > 4 and normalize_ipa(item.get("phonetic", "")).endswith("ɪŋ"):
            if not contains_block(blocks, "ing"):
                add_issue(issues, item, "WARN", "ing_not_grouped", "`-ing` 词尾没有作为整体分块。")

        if lowered.endswith("e") and contains_block(blocks, "e"):
            for block, sound, item_type in zip(blocks, sounds or [""] * len(blocks), types):
                if block.lower() == "e" and item_type == "silent" and sound:
                    add_issue(issues, item, "ERROR", "silent_e_has_sound", "静音 e 被标为 silent，但仍有分块音标。")

        if "ph" in lowered and contains_block(blocks, "ph") and sounds:
            for block, sound in zip(blocks, sounds):
                if block.lower() == "ph" and normalize_ipa(sound) != "f":
                    add_issue(issues, item, "WARN", "ph_sound_suspicious", "`ph` 作为整体分块时，音标不是 /f/。")

        if lowered in overrides and not sounds:
            add_issue(issues, item, "ERROR", "override_without_sounds", "覆盖表单词没有生成可靠分块音标。")

    for sample, expected_blocks in STRICT_SAMPLES.items():
        item = by_word.get(sample)
        if item is None:
            add_issue(issues, {"word": sample}, "ERROR", "strict_sample_missing", "重点回归样本不存在。")
            continue
        actual = [block.lower() for block in item.get("blocks", [])]
        if actual != expected_blocks:
            add_issue(issues, item, "ERROR", "strict_sample_blocks", f"重点样本分块不符合预期：{expected_blocks}")
        if not item.get("sounds"):
            add_issue(issues, item, "ERROR", "strict_sample_without_sounds", "重点样本必须有可靠分块音标。")

    OUT_CSV.parent.mkdir(parents=True, exist_ok=True)
    with OUT_CSV.open("w", encoding="utf-8-sig", newline="") as f:
        fieldnames = ["级别", "规则", "单词", "音标", "分块", "分块音标", "类型", "说明"]
        writer = csv.DictWriter(f, fieldnames=fieldnames)
        writer.writeheader()
        writer.writerows(issues)

    counts = Counter(issue["级别"] for issue in issues)
    rule_counts = Counter(issue["规则"] for issue in issues)
    top_rules = "\n".join(f"- `{rule}`：{count}" for rule, count in rule_counts.most_common(12)) or "- 无"
    sample_rows = "\n".join(
        f"- `{issue['级别']}` `{issue['单词']}`：{issue['说明']}"
        for issue in issues[:30]
    ) or "- 无"
    OUT_MD.write_text(
        "\n".join(
            [
                "# 自然拼读质量报告",
                "",
                f"- 总词数：{len(words)}",
                f"- ERROR：{counts.get('ERROR', 0)}",
                f"- WARN：{counts.get('WARN', 0)}",
                f"- 问题清单：`{OUT_CSV}`",
                "",
                "## 规则统计",
                "",
                top_rules,
                "",
                "## 前 30 条问题",
                "",
                sample_rows,
                "",
                "## 说明",
                "",
                "- `ERROR` 表示数据或重点回归样本必须修复。",
                "- `WARN` 表示自然拼读教学质量可疑，需要逐批补规则或覆盖表。",
                "- 本检查不等于人工词典校对，只负责发现高风险拆分和对齐问题。",
            ]
        ),
        encoding="utf-8",
    )

    print(json.dumps({"words": len(words), "errors": counts.get("ERROR", 0), "warnings": counts.get("WARN", 0)}, ensure_ascii=False, indent=2))
    return 1 if counts.get("ERROR", 0) else 0


if __name__ == "__main__":
    raise SystemExit(main())
