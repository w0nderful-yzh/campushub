---
name: exam-paper-image-processing
description: Crop and extract exam paper diagrams (示意图) from full-page PNG/JPG images, update structured JSON with cropped image references, and fix frontend display issues.
---

# Exam Paper Image Processing

This skill handles extracting specific diagrams/figures from exam paper page images, cropping them to show only the relevant diagram (not the full page), and updating the structured JSON data to reference the cropped images.

## When to Use

- User reports "示意图被忽略了" (diagrams being ignored) in exam simulation features
- User says cropped images are blank or show full pages instead of specific diagrams
- User needs to add diagram references to exam question JSON files
- Debugging why exam images don't display correctly in the frontend

## Workflow

### 1. Understand the Data Structure

Exam papers are stored in:
```
materials/exam-papers/
├── images/
│   ├── 2017/
│   │   ├── 4.png          # Full page images
│   │   ├── 4_crop_model_machine.png  # Cropped diagrams
│   │   └── ...
│   ├── 2018/
│   └── ...
└── structured/
    ├── 2017.json          # Question data with image references
    ├── 2018.json
    └── ...
```

Each structured JSON has questions with optional `source_images`:
```json
{
  "id": "part1-1",
  "question": "...",
  "source_images": [
    {
      "filename": "4_crop_model_machine.png",
      "label": "原卷试题图页 1"
    }
  ]
}
```

### 2. Image Processing Steps

**Read image dimensions first:**
```python
# For PNG - read IHDR chunk
python3 -c "
import struct
with open('path/to/image.png', 'rb') as f:
    f.read(16)  # Skip header + IHDR chunk type + length
    w, h = struct.unpack('>II', f.read(8))
    print(f'{w}x{h}')
"
```

**Crop using sips (macOS):**
```bash
# Syntax: sips -c pixelHeight pixelWidth offsetX offsetY input --out output
sips -c 300 450 --cropOffset 380 0 materials/exam-papers/images/2023/6.png --out materials/exam-papers/images/2023/6_crop_cpu_arch.png
```

**Or use PIL (if available):**
```python
from PIL import Image
img = Image.open('path/to/image.png')
# Crop: (left, upper, right, lower)
cropped = img.crop((x, y, x+width, y+height))
cropped.save('output.png')
```

### 3. Update Structured JSON

After cropping, update the question's `source_images` array:
```json
{
  "id": "part2-3",
  "question": "画出CPU的组成框图...",
  "source_images": [
    {
      "filename": "6_crop_cpu_arch.png",
      "label": "CPU组成框图"
    }
  ]
}
```

### 4. Common Pitfalls & Debugging

**Problem: Cropped images are blank**
- Cause: Wrong crop coordinates or image format issues
- Fix: Verify dimensions first, test with known coordinates
- Check if source image is actually PNG (some are JPG disguised as PNG)

**Problem: Images show full page, not specific diagram**
- Cause: Crop region too large or coordinates wrong
- Fix: Identify exact diagram position, crop tighter

**Problem: Frontend shows white/blank on first load, works on refresh**
- Cause: Image loading race condition or caching issue
- Fix: Check frontend image loading logic, add proper loading states

**Problem: Years 2017-2021 have no diagrams**
- Cause: These years may not have had diagrams in original papers, OR diagrams weren't extracted yet
- Fix: Manually inspect source PDFs/images to verify

### 5. Verification Checklist

After processing:
- [ ] All cropped images actually exist in filesystem
- [ ] JSON `source_images` filenames match actual files
- [ ] Cropped images show ONLY the diagram, not full page
- [ ] Frontend displays images without blank/white issues
- [ ] No duplicate `source_images` entries in JSON

## Example Session Pattern

Typical user messages that trigger this skill:
1. "试卷里的示意图直接被忽略了" → Start full processing
2. "图片截取了试卷完整的一页" → Fix crop coordinates
3. "截取的示意图都是空白的" → Debug image processing
4. "只有XX年的可以正常显示" → Check per-year image availability
5. "前端点击页面还需要能放大观看" → Add zoom functionality
