from __future__ import annotations

import csv
import json
import re
from pathlib import Path

ROOT = Path(__file__).resolve().parents[1]
WORDS_CSV = ROOT / "词汇" / "单词" / "00_单词汇总_按字母排序.csv"
WORDS_JSON = ROOT / "app" / "src" / "main" / "assets" / "words.json"
REPORT_CSV = ROOT / "词汇" / "单词" / "00_词库自检问题清单.csv"
REPORT_MD = ROOT / "词汇" / "单词" / "00_词库自检报告.md"

IPA_ALLOWED = re.compile(r"^[A-Za-zɒɑæʌəɜɛɪʊɔθðʃʒŋɡːˈˌ/;().,\-\s]+$")
CJK_SPACES = re.compile(r"[\u4e00-\u9fff]\s+[\u4e00-\u9fff]")
ALLOW_EMPTY_PHONETIC = {"P.E.", "VCD"}
PURE_CJK_MEANING = re.compile(r"^[\u4e00-\u9fff]{4,}$")
COMMON_GLUE_ERRORS = [
    "商店买东西",
    "冷的感冒",
    "红色红色的",
    "蓝色蓝色的",
    "高的高地",
    "快的快地",
    "早的早地",
    "黑暗黑暗的",
    "照料关心",
    "拍手鼓掌",
    "弄干净清洁的",
    "亲密的关闭",
    "厨师烹调",
    "盖子覆盖",
    "十字形的东西越过",
    "乱扔杂物垃圾",
    "金属金属制成的",
    "牛奶挤奶",
    "橘子橘色的",
    "在外面外面的",
    "打电话电话",
    "安全的保险柜",
    "成直线地",
]
REPEATED_MEANING_SEGMENT = re.compile(r"^([\u4e00-\u9fff]{2,4})\1")


def normalize_word(value: str) -> str:
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


def add_issue(issues: list[dict], row_no: int | str, word: str, field: str, level: str, message: str) -> None:
    issues.append(
        {
            "位置": row_no,
            "单词": word,
            "字段": field,
            "级别": level,
            "问题": message,
        }
    )


def audit_csv(issues: list[dict]) -> list[dict]:
    rows: list[dict] = []
    seen: dict[str, int] = {}
    with WORDS_CSV.open("r", encoding="utf-8-sig", newline="") as f:
        reader = csv.DictReader(f)
        for row_no, row in enumerate(reader, start=2):
            rows.append(row)
            word = row["单词"].strip()
            phonetic = row["音标"].strip()
            pos = row["词性"].strip()
            meaning = row["中文"].strip()
            key = word.lower()

            if key in seen:
                add_issue(issues, row_no, word, "单词", "error", f"与第 {seen[key]} 行大小写去重后重复")
            else:
                seen[key] = row_no

            if not word:
                add_issue(issues, row_no, word, "单词", "error", "单词为空")
            if not phonetic and word in ALLOW_EMPTY_PHONETIC:
                pass
            elif not phonetic:
                add_issue(issues, row_no, word, "音标", "error", "音标为空")
            elif not (phonetic.startswith("/") and phonetic.endswith("/")):
                add_issue(issues, row_no, word, "音标", "error", "音标未用 /.../ 包裹")
            elif not IPA_ALLOWED.match(phonetic):
                add_issue(issues, row_no, word, "音标", "warn", "音标含非常规字符")

            ipa = normalize_ipa(phonetic)
            letters = normalize_word(word)
            is_plain_lower_word = re.fullmatch(r"[a-z]+", word) is not None
            if is_plain_lower_word and letters.endswith("ed") and ipa and not ipa.endswith(("t", "d", "ɪd", "id")):
                add_issue(issues, row_no, word, "音标", "warn", "-ed 词尾与音标结尾不匹配")
            if is_plain_lower_word and letters.endswith("ing") and ipa and not ipa.endswith("ɪŋ"):
                add_issue(issues, row_no, word, "音标", "warn", "-ing 词尾与音标结尾不匹配")

            if not pos:
                add_issue(issues, row_no, word, "词性", "warn", "词性为空")
            if not meaning:
                add_issue(issues, row_no, word, "中文", "error", "中文释义为空")
            if CJK_SPACES.search(meaning):
                add_issue(issues, row_no, word, "中文", "warn", "中文释义疑似残留 OCR 空格")
            if re.search(r"[A-Za-z]{3,}", meaning):
                add_issue(issues, row_no, word, "中文", "warn", "中文释义混入英文串")
    return rows


def audit_json(issues: list[dict]) -> None:
    if not WORDS_JSON.exists():
        add_issue(issues, "words.json", "", "文件", "error", "words.json 不存在")
        return
    words = json.loads(WORDS_JSON.read_text(encoding="utf-8"))
    for idx, item in enumerate(words, start=1):
        word = item.get("word", "")
        blocks = item.get("blocks", [])
        sounds = item.get("sounds", [])
        types = item.get("types", [])
        phonetic = item.get("phonetic", "")

        if normalize_word("".join(blocks)) != normalize_word(word):
            add_issue(issues, f"words.json:{idx}", word, "blocks", "error", "分块拼接后不等于原单词")
        if len(blocks) != len(types):
            add_issue(issues, f"words.json:{idx}", word, "types", "error", "types 数量与 blocks 不一致")
        if sounds and len(blocks) != len(sounds):
            add_issue(issues, f"words.json:{idx}", word, "sounds", "error", "sounds 数量与 blocks 不一致")
        if sounds:
            merged = "".join(normalize_ipa(sound) for sound in sounds)
            target = normalize_ipa(phonetic)
            if merged != target:
                add_issue(issues, f"words.json:{idx}", word, "sounds", "error", "分块音标拼接后与整词音标不一致")
        audit_meaning_semantics(issues, idx, item)


def audit_meaning_semantics(issues: list[dict], idx: int, item: dict) -> None:
    word = item.get("word", "")
    meaning = item.get("meaning", "").strip()

    if meaning in COMMON_GLUE_ERRORS:
        add_issue(
            issues,
            f"words.json:{idx}",
            word,
            "meaning",
            "error",
            "中文释义明显是多个义项直接拼接，当前题面会误导答题"
        )
        return

    if PURE_CJK_MEANING.fullmatch(meaning) and REPEATED_MEANING_SEGMENT.search(meaning):
        add_issue(
            issues,
            f"words.json:{idx}",
            word,
            "meaning",
            "warn",
            "中文释义前后重复，疑似多个义项或 OCR 粘连"
        )


def write_reports(issues: list[dict], row_count: int) -> None:
    with REPORT_CSV.open("w", encoding="utf-8-sig", newline="") as f:
        writer = csv.DictWriter(f, fieldnames=["位置", "单词", "字段", "级别", "问题"])
        writer.writeheader()
        writer.writerows(issues)

    error_count = sum(1 for item in issues if item["级别"] == "error")
    warn_count = sum(1 for item in issues if item["级别"] == "warn")
    lines = [
        "# 词库自检报告",
        "",
        f"- 检查词条数：{row_count}",
        f"- error：{error_count}",
        f"- warn：{warn_count}",
        f"- 问题清单：`{REPORT_CSV.name}`",
        "",
        "## 前 80 条问题",
        "",
        "| 位置 | 单词 | 字段 | 级别 | 问题 |",
        "|---|---|---|---|---|",
    ]
    for item in issues[:80]:
        lines.append(f"| {item['位置']} | {item['单词']} | {item['字段']} | {item['级别']} | {item['问题']} |")
    REPORT_MD.write_text("\n".join(lines) + "\n", encoding="utf-8")


def main() -> int:
    issues: list[dict] = []
    rows = audit_csv(issues)
    audit_json(issues)
    write_reports(issues, len(rows))
    print(json.dumps({"rows": len(rows), "issues": len(issues)}, ensure_ascii=False, indent=2))
    return 1 if any(item["级别"] == "error" for item in issues) else 0


if __name__ == "__main__":
    raise SystemExit(main())
