from __future__ import annotations
from typing import Dict, Tuple, Optional

import os
import torch
from transformers import AutoTokenizer, AutoModelForSequenceClassification


class UnsmileService:
    def __init__(
        self,
        model_name: str = "smilegate-ai/kor_unsmile",
        device: Optional[str] = None,
        threshold: float = 0.75,
        clean_label_names: Tuple[str, ...] = ("clean", "neutral"),
    ):
        self.device = device or ("cuda" if torch.cuda.is_available() else "cpu")
        self.threshold = float(os.getenv("UNSMILE_THRESHOLD", threshold))
        self.clean_label_names = tuple(clean_label_names)

        self.tokenizer = AutoTokenizer.from_pretrained(model_name)
        self.model = AutoModelForSequenceClassification.from_pretrained(model_name).to(self.device)
        self.model.eval()

        self.labels = self.model.config.id2label if hasattr(self.model.config, "id2label") else None

    @torch.inference_mode()
    def moderate(self, text: str) -> Dict:
        text = (text or "").strip()
        if not text:
            return {"toxic": False, "top_label": "clean", "risk_max": 0.0, "scores": {}}

        inputs = self.tokenizer(text, return_tensors="pt", truncation=True, max_length=256).to(self.device)
        logits = self.model(**inputs).logits.squeeze(0)
        scores = torch.sigmoid(logits).detach().cpu().tolist()

        if self.labels:
            named = {str(self.labels[i]): float(scores[i]) for i in range(len(scores))}
        else:
            named = {f"label_{i}": float(scores[i]) for i in range(len(scores))}

        # ✅ 핵심: 'clean/neutral' 같은 안전 라벨은 toxic 판정에서 제외해야 오탐이 줄어듦
        risk_scores = {k: v for k, v in named.items() if k.lower() not in set(n.lower() for n in self.clean_label_names)}

        if risk_scores:
            risk_label = max(risk_scores, key=risk_scores.get)
            risk_max = float(risk_scores[risk_label])
        else:
            risk_label = "clean"
            risk_max = 0.0

        toxic = risk_max >= self.threshold

        return {
            "toxic": bool(toxic),
            "top_label": risk_label if toxic else "clean",
            "risk_max": risk_max,
            "scores": named,
        }
