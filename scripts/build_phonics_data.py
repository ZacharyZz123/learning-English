from __future__ import annotations

import csv
import json
import re
from functools import lru_cache
from pathlib import Path

ROOT = Path(__file__).resolve().parents[1]
WORDS_CSV = ROOT / "词汇" / "单词" / "00_单词汇总_按字母排序.csv"
RULES_JSON = ROOT / "app" / "src" / "main" / "assets" / "phonics_rules.json"
OVERRIDES_JSON = ROOT / "app" / "src" / "main" / "assets" / "phonics_overrides.json"
WORDS_JSON = ROOT / "app" / "src" / "main" / "assets" / "words.json"
REPORT_JSON = ROOT / "词汇" / "自然拼读校验报告.json"

CONSONANT_SOUNDS = {
    "b": [("b", "consonant")],
    "c": [("k", "consonant"), ("s", "consonant")],
    "d": [("d", "consonant")],
    "f": [("f", "consonant")],
    "g": [("g", "consonant"), ("dʒ", "consonant")],
    "h": [("h", "consonant")],
    "j": [("dʒ", "consonant")],
    "k": [("k", "consonant")],
    "l": [("l", "consonant")],
    "m": [("m", "consonant")],
    "n": [("n", "consonant")],
    "p": [("p", "consonant")],
    "q": [("kw", "consonant")],
    "r": [("r", "consonant")],
    "s": [("s", "consonant"), ("z", "consonant")],
    "t": [("t", "consonant")],
    "v": [("v", "consonant")],
    "w": [("w", "consonant")],
    "x": [("ks", "consonant"), ("gz", "consonant")],
    "y": [("j", "consonant"), ("i", "vowel"), ("aɪ", "vowel")],
    "z": [("z", "consonant")],
}

VOWEL_SOUNDS = {
    "a": [("æ", "vowel"), ("ə", "vowel"), ("eɪ", "vowel"), ("ɑː", "vowel"), ("ɔː", "vowel")],
    "e": [("e", "vowel"), ("ɪ", "vowel"), ("iː", "vowel"), ("ə", "vowel")],
    "i": [("ɪ", "vowel"), ("aɪ", "vowel"), ("i", "vowel")],
    "o": [("ɒ", "vowel"), ("əʊ", "vowel"), ("ʌ", "vowel"), ("uː", "vowel"), ("ə", "vowel")],
    "u": [("ʌ", "vowel"), ("juː", "vowel"), ("uː", "vowel"), ("ʊ", "vowel"), ("ə", "vowel")],
}


def strip_slashes(value: str) -> str:
    value = value.strip()
    if value.startswith("/") and value.endswith("/"):
        value = value[1:-1]
    return value


def normalize_ipa(value: str) -> str:
    value = strip_slashes(value)
    value = value.replace("ɡ", "g")
    value = value.replace("ˈ", "").replace("ˌ", "")
    value = value.replace(".", "").replace(" ", "")
    return value


def wrap_ipa(value: str) -> str:
    return f"/{value}/" if value else ""


def load_rules() -> list[dict]:
    rules = json.loads(RULES_JSON.read_text(encoding="utf-8"))
    result = []
    for rule in rules:
        sounds = rule.get("sounds") or [rule.get("sound") or rule.get("hint")]
        for sound in sounds:
            if not sound:
                continue
            result.append(
                {
                    "pattern": rule["pattern"].lower(),
                    "sound": normalize_ipa(sound),
                    "type": rule.get("type") or rule.get("category") or "consonant",
                }
            )
    return sorted(result, key=lambda x: len(x["pattern"]), reverse=True)


def load_overrides() -> dict[str, dict]:
    if not OVERRIDES_JSON.exists():
        return {}
    return json.loads(OVERRIDES_JSON.read_text(encoding="utf-8"))


def row_to_word(row: dict, blocks: list[str], sounds: list[str], types: list[str]) -> dict:
    return {
        "word": row["单词"].strip(),
        "phonetic": row["音标"].strip(),
        "partOfSpeech": row["词性"].strip(),
        "meaning": row["中文"].strip(),
        "source": row["来源"].strip(),
        "blocks": blocks,
        "sounds": sounds,
        "types": types,
    }


def word_key(word: str) -> str:
    return re.sub(r"[^a-z]", "", word.lower())


def letters_only(word: str) -> bool:
    return re.fullmatch(r"[A-Za-z]+", word) is not None


def override_result(word: str, phonetic: str, override: dict) -> tuple[list[str], list[str], list[str]] | None:
    blocks = override.get("blocks") or []
    sounds = override.get("sounds") or []
    types = override.get("types") or ["irregular"] * len(blocks)
    if len(blocks) != len(sounds) or len(blocks) != len(types):
        return None
    if word_key("".join(blocks)) != word_key(word):
        return None
    merged = "".join(normalize_ipa(sound) for sound in sounds)
    target = normalize_ipa(phonetic)
    if merged != target:
        return None
    return blocks, sounds, types


def parse_word(word: str, phonetic: str, rules: list[dict]) -> tuple[list[str], list[str], list[str]] | None:
    if not letters_only(word):
        return None
    source = word.lower()
    target = normalize_ipa(phonetic)
    if not target:
        return None

    @lru_cache(maxsize=None)
    def dfs(pos: int, sound_pos: int) -> tuple[tuple[str, str, str], ...] | None:
        if pos == len(source):
            return tuple() if sound_pos == len(target) else None

        candidates: list[tuple[str, str, str]] = []
        if source[pos] == "e" and pos == len(source) - 1:
            candidates.append(("e", "", "silent"))

        for rule in rules:
            pattern = rule["pattern"]
            if "_" in pattern:
                continue
            if source.startswith(pattern, pos):
                candidates.append((pattern, rule["sound"], rule["type"]))

        letter = source[pos]
        for sound, item_type in VOWEL_SOUNDS.get(letter, []) + CONSONANT_SOUNDS.get(letter, []):
            candidates.append((letter, sound, item_type))

        for text, sound, item_type in candidates:
            next_sound_pos = sound_pos + len(sound)
            if sound and not target.startswith(sound, sound_pos):
                continue
            if not sound and not target.startswith("", sound_pos):
                continue
            tail = dfs(pos + len(text), next_sound_pos)
            if tail is not None:
                return ((word[pos : pos + len(text)], wrap_ipa(sound), item_type),) + tail
        return None

    result = dfs(0, 0)
    if result is None:
        return None
    blocks = [item[0] for item in result]
    sounds = [item[1] for item in result]
    types = [item[2] for item in result]
    return blocks, sounds, types


def visual_chunks(word: str, rules: list[dict]) -> tuple[list[str], list[str], list[str]]:
    if not letters_only(word):
        return [word], [], ["word"]
    source = word.lower()
    blocks: list[str] = []
    types: list[str] = []
    pos = 0
    while pos < len(source):
        matched = None
        for rule in rules:
            pattern = rule["pattern"]
            if "_" in pattern:
                continue
            if source.startswith(pattern, pos):
                matched = rule
                break
        if matched:
            text = word[pos : pos + len(matched["pattern"])]
            blocks.append(text)
            types.append(matched["type"])
            pos += len(matched["pattern"])
            continue
        letter = source[pos]
        blocks.append(word[pos])
        if letter in "aeiou":
            types.append("vowel")
        else:
            types.append("consonant")
        pos += 1
    return blocks, [], types


def build_words() -> tuple[list[dict], dict]:
    rules = load_rules()
    overrides = load_overrides()
    words: list[dict] = []
    report = {
        "total": 0,
        "override": 0,
        "auto": 0,
        "whole_only": 0,
        "samples": {},
    }

    with WORDS_CSV.open("r", encoding="utf-8-sig", newline="") as f:
        reader = csv.DictReader(f)
        for row in reader:
            report["total"] += 1
            word = row["单词"].strip()
            phonetic = row["音标"].strip()
            lowered = word.lower()

            parsed = None
            if lowered in overrides:
                parsed = override_result(word, phonetic, overrides[lowered])
                if parsed:
                    report["override"] += 1
            if parsed is None:
                parsed = parse_word(word, phonetic, rules)
                if parsed:
                    report["auto"] += 1

            if parsed is None:
                blocks, sounds, types = visual_chunks(word, rules)
                words.append(row_to_word(row, blocks, sounds, types))
                report["whole_only"] += 1
            else:
                blocks, sounds, types = parsed
                words.append(row_to_word(row, blocks, sounds, types))

    for sample in ["box", "require", "achievement", "agreement", "action", "name", "fish", "night"]:
        hit = next((item for item in words if item["word"].lower() == sample), None)
        if hit:
            report["samples"][sample] = {
                "phonetic": hit["phonetic"],
                "blocks": hit["blocks"],
                "sounds": hit["sounds"],
                "types": hit["types"],
            }
    return words, report


def main() -> int:
    words, report = build_words()
    WORDS_JSON.write_text(json.dumps(words, ensure_ascii=False, indent=2), encoding="utf-8")
    REPORT_JSON.write_text(json.dumps(report, ensure_ascii=False, indent=2), encoding="utf-8")
    print(json.dumps(report, ensure_ascii=False, indent=2))
    return 0


if __name__ == "__main__":
    raise SystemExit(main())
