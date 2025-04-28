import matplotlib.pyplot as plt
import matplotlib.patches as patches
import numpy as np
from matplotlib.colors import LinearSegmentedColormap

def visualize_toom_cook_recursion(depth=2):
    """
    Visualize the recursive structure of Toom-Cook multiplication.
    
    Parameters:
    depth (int): The recursion depth to visualize
    """
    # Create figure
    fig, ax = plt.subplots(figsize=(12, 10))
    ax.set_xlim(0, 100)
    ax.set_ylim(0, 100)
    ax.axis('off')
    
    # Define custom colormap for the blocks
    colors = [(0.8, 0.2, 0.2), (0.2, 0.6, 0.8)]  # Red to Blue
    cmap = LinearSegmentedColormap.from_list('RedBlue', colors, N=depth+1)
    
    # Title
    ax.text(50, 95, 'Toom-Cook Recursive Multiplication Structure', 
            fontsize=16, ha='center', fontweight='bold')
    ax.text(50, 90, 'Visualization of recursive divide-and-conquer for Toom-3', 
            fontsize=12, ha='center', style='italic')
    
    # Draw the initial problem
    draw_block(ax, 10, 70, 80, 15, depth, 0, cmap)
    ax.text(50, 77, 'A × B', fontsize=14, ha='center', va='center')
    
    # Draw the evaluation points
    ax.text(50, 60, 'Evaluate at 5 points (x = 0, 1, -1, 2, ∞)', 
            fontsize=12, ha='center', fontweight='bold')
    
    # Draw the five subproblems
    positions = [(10, 45), (25, 45), (40, 45), (55, 45), (70, 45)]
    labels = ['A(0)×B(0)', 'A(1)×B(1)', 'A(-1)×B(-1)', 'A(2)×B(2)', 'A(∞)×B(∞)']
    
    for i, (pos, label) in enumerate(zip(positions, labels)):
        draw_block(ax, pos[0], pos[1], 15, 10, depth, 1, cmap)
        ax.text(pos[0] + 7.5, pos[1] + 5, label, fontsize=9, ha='center', va='center')
        
        # Connect only to the parent block (remove the redundant arrows)
        if i == 0:  # Only connect the main block to the first subproblem
            ax.annotate('', xy=(pos[0] + 7.5, pos[1] + 10), xytext=(50, 70 + 15),  # Pointing to the top edge of the parent block
                        arrowprops=dict(arrowstyle='->', color='black', lw=1))
    
    plt.tight_layout()
    plt.savefig('toom_cook_recursion.png', dpi=300, bbox_inches='tight')
    plt.close()
    print("Visualization saved as 'toom_cook_recursion.png'")

def draw_block(ax, x, y, width, height, max_depth, current_depth, cmap):
    """Draw a block representing a multiplication problem at a particular recursion depth"""
    color = cmap(current_depth / max_depth)
    
    # Draw the main block
    rect = patches.Rectangle((x, y), width, height, 
                            linewidth=1, edgecolor='black', facecolor=color, alpha=0.7)
    ax.add_patch(rect)
    
    # If we haven't reached max depth, show subdivision
    if current_depth < max_depth:
        # Draw the 5 subdivisions representing the evaluation points
        sub_width = width / 5
        for i in range(5):
            sub_x = x + i * sub_width
            draw_block(ax, sub_x, y - height - 12, sub_width, height * 0.8, 
                      max_depth, current_depth + 1, cmap)
            
            # Connect only from the parent block to the child blocks
            # (don't draw extra arrows inside the recursion)
            if current_depth == 0:
                ax.annotate('', xy=(sub_x + sub_width/2, y - height - 12), 
                           xytext=(x + width/2, y + height), 
                           arrowprops=dict(arrowstyle='->', color='black', lw=0.5))
    else:
        # Base case - show ellipsis
        ax.text(x + width/2, y - height/2 - 2, "...", fontsize=12, ha='center')

# Generate the visualization
visualize_toom_cook_recursion(depth=2)

