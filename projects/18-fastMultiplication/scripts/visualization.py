import matplotlib.pyplot as plt
import numpy as np
from matplotlib.patches import Rectangle, FancyArrowPatch
from matplotlib.colors import LinearSegmentedColormap
import matplotlib.gridspec as gridspec

def visualize_toom_cook():
    # Create figure with custom layout
    fig = plt.figure(figsize=(14, 16))
    gs = gridspec.GridSpec(3, 1, height_ratios=[1, 1.5, 1.5])
    
    # STEP 1: Splitting into parts
    ax1 = plt.subplot(gs[0])
    ax1.set_title("Step 1: Splitting Numbers into Parts", fontsize=16)
    ax1.axis('off')
    
    # Original numbers
    ax1.add_patch(Rectangle((0.1, 0.6), 0.8, 0.2, fc='skyblue', ec='blue', lw=2))
    ax1.add_patch(Rectangle((0.1, 0.2), 0.8, 0.2, fc='lightgreen', ec='green', lw=2))
    ax1.text(0.5, 0.7, "Number A", ha='center', va='center', fontsize=14)
    ax1.text(0.5, 0.3, "Number B", ha='center', va='center', fontsize=14)
    
    # Split parts
    for i in range(3):
        ax1.add_patch(Rectangle((0.1 + i*0.3, 0.9), 0.25, 0.1, fc='royalblue', ec='blue', lw=2))
        ax1.text(0.225 + i*0.3, 0.95, f"A{2-i}", ha='center', va='center', fontsize=12)
        ax1.add_patch(FancyArrowPatch((0.225 + i*0.3, 0.8), (0.225 + i*0.3, 0.9), 
                                 arrowstyle='->', color='blue', mutation_scale=20, lw=2))
        
        ax1.add_patch(Rectangle((0.1 + i*0.3, 0.0), 0.25, 0.1, fc='lightseagreen', ec='green', lw=2))
        ax1.text(0.225 + i*0.3, 0.05, f"B{2-i}", ha='center', va='center', fontsize=12)
        ax1.add_patch(FancyArrowPatch((0.225 + i*0.3, 0.2), (0.225 + i*0.3, 0.1), 
                                 arrowstyle='->', color='green', mutation_scale=20, lw=2))
    
    # STEP 2: Polynomial representation and evaluation
    ax2 = plt.subplot(gs[1])
    ax2.set_title("Step 2: Polynomial Representation and Evaluation", fontsize=16)
    ax2.axis('off')
    
    # Draw polynomials
    ax2.text(0.1, 0.9, r"$A(x) = A_2 x^2 + A_1 x + A_0$", fontsize=14, color='blue')
    ax2.text(0.1, 0.8, r"$B(x) = B_2 x^2 + B_1 x + B_0$", fontsize=14, color='green')
    
    # Evaluation points box
    ax2.add_patch(Rectangle((0.1, 0.5), 0.8, 0.2, fc='mistyrose', ec='red', lw=2, alpha=0.7))
    ax2.text(0.5, 0.6, "Evaluate at 5 points: x = 0, 1, -1, 2, ∞", ha='center', va='center', fontsize=14)
    
    # Evaluation results
    points = ["0", "1", "-1", "2", "∞"]
    for i, point in enumerate(points):
        y_pos = 0.4 - i*0.07
        ax2.text(0.2, y_pos, f"A({point}) = ", fontsize=12, color='blue')
        ax2.text(0.4, y_pos, f"value A{i}", fontsize=12, color='blue')
        ax2.text(0.6, y_pos, f"B({point}) = ", fontsize=12, color='green')
        ax2.text(0.8, y_pos, f"value B{i}", fontsize=12, color='green')
    
    # Multiply arrow
    ax2.add_patch(FancyArrowPatch((0.5, 0.05), (0.5, -0.05), 
                             arrowstyle='->', color='purple', mutation_scale=20, lw=2))
    ax2.text(0.65, 0.0, "Point-wise multiplication", fontsize=12, color='purple')
    
    # STEP 3: Multiplication and Interpolation
    ax3 = plt.subplot(gs[2])
    ax3.set_title("Step 3: Multiplication, Interpolation, and Recomposition", fontsize=16)
    ax3.axis('off')
    
    # Multiplication results
    for i, point in enumerate(points):
        y_pos = 0.9 - i*0.07
        ax3.text(0.2, y_pos, f"C({point}) = A({point}) × B({point}) = ", fontsize=12, color='purple')
        ax3.text(0.7, y_pos, f"product C{i}", fontsize=12, color='purple')
    
    # Interpolation box
    ax3.add_patch(Rectangle((0.1, 0.5), 0.8, 0.1, fc='lavender', ec='purple', lw=2, alpha=0.7))
    ax3.text(0.5, 0.55, "Interpolate to find coefficients of C(x)", ha='center', va='center', fontsize=14)
    
    # Result polynomial
    ax3.text(0.1, 0.4, r"$C(x) = C_4 x^4 + C_3 x^3 + C_2 x^2 + C_1 x + C_0$", fontsize=14, color='darkviolet')
    
    # Final recomposition
    ax3.add_patch(Rectangle((0.2, 0.2), 0.6, 0.1, fc='plum', ec='darkviolet', lw=2))
    ax3.text(0.5, 0.25, "Final Product A × B", ha='center', va='center', fontsize=14)
    
    plt.tight_layout()
    plt.savefig("toom_cook_visualization.png", dpi=300, bbox_inches='tight')
    plt.close()
    
    print("Generated visualization saved as 'toom_cook_visualization.png'")

def visualize_comparison():
    # Create figure
    fig, ax = plt.subplots(figsize=(10, 6))
    
    # Data
    x = np.linspace(1, 10, 100)  # Number size (log scale)
    standard = x**2
    karatsuba = x**1.585
    toom3 = x**1.465
    toom4 = x**1.404
    fft = x * np.log(x)
    
    # Normalize for visualization
    max_val = np.max(standard)
    standard = standard / max_val
    karatsuba = karatsuba / max_val
    toom3 = toom3 / max_val
    toom4 = toom4 / max_val
    fft = fft / max_val
    
    # Plotting
    ax.plot(x, standard, 'r-', linewidth=2, label='Standard O(n²)')
    ax.plot(x, karatsuba, 'g-', linewidth=2, label='Karatsuba O(n^1.585)')
    ax.plot(x, toom3, 'b-', linewidth=2, label='Toom-3 O(n^1.465)')
    ax.plot(x, toom4, 'c-', linewidth=2, label='Toom-4 O(n^1.404)')
    ax.plot(x, fft, 'm-', linewidth=2, label='FFT O(n log n)')
    
    # Highlight Toom-Cook efficiency region
    ax.axvspan(3.5, 7, alpha=0.2, color='yellow')
    ax.text(5.25, 0.5, "Toom-Cook\nEfficiency Region", 
            ha='center', va='center', bbox=dict(boxstyle='round', fc='white', alpha=0.7))
    
    # Add labels and title
    ax.set_xlabel('Number Size (log scale)', fontsize=12)
    ax.set_ylabel('Relative Time Complexity', fontsize=12)
    ax.set_title('Comparison of Multiplication Algorithm Complexities', fontsize=14)
    ax.legend(loc='upper left')
    ax.grid(True, linestyle='--', alpha=0.7)
    
    plt.tight_layout()
    plt.savefig("multiplication_comparison.png", dpi=300)
    plt.close()
    
    print("Generated comparison chart saved as 'multiplication_comparison.png'")

# Generate both visualizations
visualize_toom_cook()
visualize_comparison()
