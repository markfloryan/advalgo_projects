import matplotlib.pyplot as plt
import matplotlib.patches as patches
import numpy as np
from matplotlib.path import Path

# Set up the figure with appropriate dimensions
plt.figure(figsize=(14, 10))
plt.subplots_adjust(left=0.05, right=0.95, top=0.95, bottom=0.05)

# Turn off axis ticks for cleaner visualization
ax = plt.gca()
ax.set_xlim(0, 10)
ax.set_ylim(0, 10)
ax.axis('off')

# Title
plt.title('Toom-Cook Algorithm (Toom-3) Visualization', fontsize=16)

# Define improved colors
COLORS = {
    'box': '#6baed6',  # lighter blue
    'arrow': '#000000',  # black arrows instead of red
    'text': '#2c3e50',  # dark blue
    'highlight': '#fd8d3c',  # better orange
    'result': '#41ab5d',  # better green
    'recursion': '#9e9ac8'  # better purple
}

# Draw a box with text
def draw_box(x, y, width, height, text, color=COLORS['box'], alpha=1.0, fontsize=10):
    rect = patches.Rectangle((x, y), width, height, linewidth=1, edgecolor=color, 
                            facecolor=color, alpha=alpha)
    ax.add_patch(rect)
    plt.text(x + width/2, y + height/2, text, ha='center', va='center', 
             color=COLORS['text'], fontsize=fontsize, wrap=True)

# Draw an arrow with offset text to avoid overlapping the line
def draw_arrow(start, end, color=COLORS['arrow'], width=0.03, alpha=0.8, text=None, text_offset=(0.2, 0.2)):
    arrow = patches.FancyArrowPatch(start, end, color=color, arrowstyle='->', 
                                    linewidth=2, alpha=alpha)
    ax.add_patch(arrow)
    if text:
        mid_x = (start[0] + end[0]) / 2
        mid_y = (start[1] + end[1]) / 2
        plt.text(mid_x + text_offset[0], mid_y + text_offset[1], text, ha='center', va='center', 
                 color=COLORS['text'], fontsize=8)

# Section 1: Initial numbers
draw_box(1, 9, 2, 0.8, "Number A", alpha=0.8, fontsize=12)
draw_box(7, 9, 2, 0.8, "Number B", alpha=0.8, fontsize=12)

# Section 2: Split into 3 parts (representing as polynomials)
draw_box(0.5, 7.5, 1, 0.6, "a₂", COLORS['box'], 0.7)
draw_box(1.7, 7.5, 1, 0.6, "a₁", COLORS['box'], 0.7)
draw_box(2.9, 7.5, 1, 0.6, "a₀", COLORS['box'], 0.7)

draw_box(6.5, 7.5, 1, 0.6, "b₂", COLORS['box'], 0.7)
draw_box(7.7, 7.5, 1, 0.6, "b₁", COLORS['box'], 0.7)
draw_box(8.9, 7.5, 1, 0.6, "b₀", COLORS['box'], 0.7)

# Add polynomial notation
plt.text(2, 8.3, "A(x) = a₂x² + a₁x + a₀", fontsize=10)
plt.text(8, 8.3, "B(x) = b₂x² + b₁x + b₀", fontsize=10)

# Connect with arrows - only from the general concept, not from each part
draw_arrow((2, 9), (2, 8.6), text="Split into k=3 parts", text_offset=(0.8, 0))
draw_arrow((8, 9), (8, 8.6), text="Split into k=3 parts", text_offset=(0.8, 0))

# Section 3: Evaluation at 5 points - aligned vertically
points = ["x = 0", "x = 1", "x = -1", "x = 2", "x = ∞"]
y_pos_a = 6.2  # A evaluations
y_pos_b = 5.2  # B evaluations
x_base = 1.0
spacing = 1.8

for i, point in enumerate(points):
    # A evaluations
    x_pos = x_base + i * spacing
    draw_box(x_pos, y_pos_a, 0.8, 0.6, f"A({point.split('=')[1].strip()})", COLORS['highlight'], 0.7)
    
    # B evaluations
    draw_box(x_pos, y_pos_b, 0.8, 0.6, f"B({point.split('=')[1].strip()})", COLORS['highlight'], 0.7)
    
    # Add multiplication symbols between A and B evaluations
    plt.text(x_pos + 0.4, y_pos_a - 0.3, "×", fontsize=12, ha='center', color=COLORS['text'])

# Add arrows from polynomial representation to evaluation concept
draw_arrow((2, 7.5), (2, 6.8), text="Evaluate at points", text_offset=(0.5, 0))
draw_arrow((8, 7.5), (8, 6.8), text="Evaluate at points", text_offset=(0.5, 0))

# Section 4: Recursive multiplication
for i in range(5):
    x_pos = x_base + i * spacing
    # Result of multiplication
    draw_box(x_pos, 4.0, 0.8, 0.6, "A×B", COLORS['recursion'], 0.7)
    
    # Connect with arrows
    draw_arrow((x_pos + 0.4, y_pos_b), (x_pos + 0.4, 4.6), text="Multiply (recursively)", text_offset=(0.6, 0))
    
    # Recursion indicator - only on one path for clarity
    if i == 2:
        plt.text(x_pos + 0.4, 3.5, "...", fontsize=24, ha='center', color=COLORS['recursion'])

# Better connect the recursion to the interpolation step
recursion_arrow = patches.FancyArrowPatch((5, 3.7), (5, 3.2), 
                                         color=COLORS['recursion'], 
                                         arrowstyle='->', 
                                         linewidth=2, 
                                         alpha=0.8)
ax.add_patch(recursion_arrow)

# Section 5: Interpolation - fix positioning
plt.text(5, 3.0, "Interpolation to find coefficients of C(x) = A(x) × B(x)", fontsize=12, ha='center')

# Product polynomial
draw_box(3.5, 2.0, 3, 0.8, "C(x) = c₄x⁴ + c₃x³ + c₂x² + c₁x + c₀", COLORS['result'], 0.8, fontsize=12)

# Final result
draw_box(4, 0.8, 2, 0.8, "Final Product (A×B)", COLORS['result'], 0.9, fontsize=12)

# Connect interpolation to result with improved label
draw_arrow((5, 2.0), (5, 1.6), text="Convert polynomial to integer", text_offset=(1.0, 0))

plt.tight_layout(rect=[0, 0.05, 1, 0.95])
plt.savefig('toom3_visualization.png', dpi=300, bbox_inches='tight')
plt.show()
