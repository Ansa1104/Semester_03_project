# 🧮 Calculus Toolkit

A Java Swing desktop application built as a Semester 3 project that provides essential calculus and algebra tools in a clean, modern GUI.

---

## 📌 Features

### 1. Differentiation
- Symbolically differentiates mathematical expressions with respect to `x`
- Supports: polynomials, trig, inverse trig, hyperbolic, logarithmic, exponential functions
- Chain rule, product rule, quotient rule, and power rule applied automatically
- **Show Steps** button displays a step-by-step differentiation breakdown
- Unicode superscript rendering for clean math display (e.g. x² instead of x^2)

### 2. Polynomial Tools
- Perform **Addition**, **Subtraction**, **Multiplication**, and **Division** on polynomials
- Polynomial long division with remainder display
- Clean formatted output with superscript exponents

### 3. Evaluate f(x)
- Evaluate any mathematical function at a given value of `x`
- Supports all standard math functions: `sin`, `cos`, `tan`, `ln`, `log`, `sqrt`, `exp`, and more
- Displays result as: `f(x) = ...` and `f(value) = result`

---

## 🖥️ Tech Stack

| Technology | Details |
|---|---|
| Language | Java |
| GUI Framework | Java Swing |
| Rendering | Custom paintComponent + HTML JEditorPane |
| Build | Compile manually with `javac` |

---

## 🚀 How to Run

### Prerequisites
- Java JDK 11 or higher installed
- (Optional) An IDE like IntelliJ IDEA or Eclipse

### Steps

**Option 1 — Terminal**
```bash
# Compile
javac CalculusToolkit.java

# Run
java CalculusToolkit
```

**Option 2 — IDE**
1. Open the project folder in IntelliJ IDEA or Eclipse
2. Run `CalculusToolkit.java` as the main class

---

## 📁 Project Structure

```
CalculusToolkit/
│
├── CalculusToolkit.java       # Main entry point + Welcome screen
├── Palette.java               # (inside same file) Color theme
├── MainMenuScreen.java        # Main menu navigation
├── DifferentiationScreen.java # Differentiation UI
├── DiffEngine.java            # Symbolic differentiation logic
├── PolynomialToolsScreen.java # Polynomial menu
├── PolynomialOperationScreen  # Polynomial arithmetic UI
├── Polynomial.java            # Polynomial data model + operations
├── EvaluateScreen.java        # f(x) evaluation UI
├── FunctionEvaluator.java     # Expression parser + evaluator
│
├── First_InterfaceLogo.png    # App logo (place in root directory)
└── README.md
```

> **Note:** All classes are written in a single `CalculusToolkit.java` file.

---

## 🎨 UI Theme

| Element | Color |
|---|---|
| Background | White → Light Gray gradient |
| Accent / Panels | Deep Burgundy `#6E0D25` |
| Buttons | Mint Green `#A4E9D5` / Yellow `#FFC857` |
| Text on buttons | Burgundy on Mint |

---

## 📐 Supported Functions

| Category | Functions |
|---|---|
| Trigonometric | `sin`, `cos`, `tan`, `cot`, `sec`, `csc` |
| Inverse Trig | `arcsin`, `arccos`, `arctan`, `arccot`, `asec`, `acsc` |
| Hyperbolic | `sinh`, `cosh`, `tanh`, `asinh`, `acosh`, `atanh` |
| Logarithmic | `ln`, `log` |
| Exponential | `exp`, `e^x` |
| Other | `sqrt`, `x^n` (any power) |

---

## 👨‍💻 Authors

> Semester 3 Project  
> Subject: Object Oriented Programming / Data Structures  
> Language: Java

---

## 📄 License

This project is for educational purposes. Feel free to use or modify it for learning.