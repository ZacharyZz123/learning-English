from __future__ import annotations

import json
from pathlib import Path

ROOT = Path(__file__).resolve().parents[1]
WORDS_JSON = ROOT / "app" / "src" / "main" / "assets" / "words.json"

SAMPLES = [
    "box",
    "require",
    "achievement",
    "agreement",
    "thought",
    "fruit",
    "shepherd",
    "interested",
    "watermelon",
    "pass",
    "professional",
    "classic",
    "I'll",
    "gradually",
    "cleaned",
    "magazine",
]

EXPECTED_BLOCKS = {
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
    "gradually": ["gr", "a", "du", "al", "ly"],
    "i'll": ["i", "'ll"],
}

STRICT_WITH_SOUNDS = set(EXPECTED_BLOCKS)


def normalize_word(value: str) -> str:
    return "".join(ch.lower() for ch in value if ch.isalpha())


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


def main() -> int:
    words = json.loads(WORDS_JSON.read_text(encoding="utf-8"))
    by_word = {item["word"].lower(): item for item in words}
    failed = 0
    for sample in SAMPLES:
        item = by_word.get(sample.lower())
        if item is None:
            print(f"FAIL {sample}: missing")
            failed += 1
            continue
        blocks = item.get("blocks", [])
        sounds = item.get("sounds", [])
        types = item.get("types", [])
        word_ok = normalize_word("".join(blocks)) == normalize_word(item["word"])
        count_ok = len(blocks) == len(types) and (not sounds or len(blocks) == len(sounds))
        expected_blocks = EXPECTED_BLOCKS.get(sample.lower())
        blocks_ok = expected_blocks is None or [block.lower() for block in blocks] == expected_blocks
        sounds_required_ok = sample.lower() not in STRICT_WITH_SOUNDS or bool(sounds)
        sound_ok = True
        if sounds:
            sound_ok = "".join(normalize_ipa(sound) for sound in sounds) == normalize_ipa(item["phonetic"])
        status = "OK" if word_ok and count_ok and blocks_ok and sounds_required_ok and sound_ok else "FAIL"
        if status != "OK":
            failed += 1
        print(
            f"{status} {item['word']}: {item['phonetic']} | "
            f"{' | '.join(blocks)} | {' '.join(sounds)} | {' '.join(types)}"
        )
    return 1 if failed else 0


if __name__ == "__main__":
    raise SystemExit(main())
