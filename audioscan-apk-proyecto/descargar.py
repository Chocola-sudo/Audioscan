#!/usr/bin/env python3
"""Descarga a disco la librería (transformers.js + wasm) y los modelos Whisper.
Uso:  python3 descargar.py lib tiny base      (small es opcional: ~250 MB)"""
import json, os, re, sys, urllib.request

RAIZ = os.path.abspath(os.environ.get("DESTINO") or os.path.dirname(os.path.abspath(__file__)))
UA = {"User-Agent": "Mozilla/5.0 AudioScan"}

def get(url):
    return urllib.request.urlopen(urllib.request.Request(url, headers=UA), timeout=60)

def bajar(url, destino):
    os.makedirs(os.path.dirname(destino), exist_ok=True)
    try:
        total = int(get(url).headers.get("Content-Length", 0))
    except Exception:
        total = 0
    if total and os.path.exists(destino) and os.path.getsize(destino) == total:
        print("  ya existe:", os.path.relpath(destino, RAIZ)); return
    tmp = destino + ".part"
    with get(url) as r, open(tmp, "wb") as f:
        hecho = 0
        while True:
            b = r.read(1 << 20)
            if not b: break
            f.write(b); hecho += len(b)
            print(f"\r  {os.path.relpath(destino, RAIZ)} {hecho/1e6:.1f} MB", end="", flush=True)
    os.replace(tmp, destino); print()

def lib():
    base = "https://cdn.jsdelivr.net/npm/@xenova/transformers@2.17.2/dist/transformers.min.js"
    dest = os.path.join(RAIZ, "lib", "transformers.min.js")
    bajar(base, dest)
    txt = open(dest, encoding="utf-8", errors="ignore").read()
    m = re.search(r"onnxruntime-web@([0-9][0-9.]*)", txt)
    ver = m.group(1) if m else "1.14.0"
    print("  onnxruntime-web", ver)
    for w in ["ort-wasm.wasm", "ort-wasm-simd.wasm", "ort-wasm-threaded.wasm", "ort-wasm-simd-threaded.wasm"]:
        try:
            bajar(f"https://cdn.jsdelivr.net/npm/onnxruntime-web@{ver}/dist/{w}", os.path.join(RAIZ, "lib", w))
        except Exception as e:
            print("  (omitido)", w, e)

def modelo(nombre):
    repo = f"Xenova/whisper-{nombre}"
    info = json.load(get(f"https://huggingface.co/api/models/{repo}"))
    for s in info["siblings"]:
        f = s["rfilename"]
        if f.startswith(".") or f.lower().startswith("readme"): continue
        if f.startswith("onnx/") and not f.endswith("_quantized.onnx"): continue
        bajar(f"https://huggingface.co/{repo}/resolve/main/{f}", os.path.join(RAIZ, "models", repo, f))

if __name__ == "__main__":
    for a in (sys.argv[1:] or ["lib", "tiny", "base"]):
        print("==", a)
        lib() if a == "lib" else modelo(a)
    print("Listo.")
