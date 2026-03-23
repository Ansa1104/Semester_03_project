import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.*;
import java.util.ArrayList;
import java.util.Stack;
import javax.swing.*;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class CalculusToolkit extends JFrame {
    JLabel logo;
    JButton startBtn;

    public CalculusToolkit() {

        setTitle("Calculus Toolkit");
        setSize(550, 900);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setResizable(true);

        JPanel panel = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);

                Graphics2D g2 = (Graphics2D) g;
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                GradientPaint gradient = new GradientPaint(
                        0, 0, Palette.BG_TOP,
                        0, getHeight(), Palette.BG_BOTTOM);
                g2.setPaint(gradient);
                g2.fillRect(0, 0, getWidth(), getHeight());
            }
        };

        panel.setLayout(null);
        add(panel);

        // LOGO (safe fallback if images missing)
        try {
            ImageIcon icon = new ImageIcon("First_InterfaceLogo.png");
            Image scaled = icon.getImage().getScaledInstance(330, 260, Image.SCALE_SMOOTH);
            logo = new JLabel(new ImageIcon(scaled));
            logo.setSize(330, 260);
            panel.add(logo);
            try {
                setIconImage(new ImageIcon("First_Interface.jpeg").getImage());
            } catch (Exception ex) {
                // ignore missing icon image
            }
        } catch (Exception e) {
            logo = new JLabel("CALCULUS TOOLKIT", SwingConstants.CENTER);
            logo.setFont(new Font("Segoe UI", Font.BOLD, 28));
            logo.setSize(330, 80);
            panel.add(logo);
        }

        // START BUTTON
        startBtn = new JButton("START") {
            @Override
            protected void paintComponent(Graphics g) {

                // 1 — FIRST draw background
                Graphics2D g2 = (Graphics2D) g;
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                Color c;
                if (getModel().isPressed())
                    c = Palette.WELCOME_ACCENT.darker();
                else if (getModel().isRollover())
                    c = Palette.WELCOME_ACCENT.brighter();
                else
                    c = Palette.WELCOME_ACCENT;

                g2.setColor(c);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 50, 50);

                // 2 — THEN draw the button text (MANUALLY)
                super.paintComponent(g);
            }
        };

        startBtn.setFont(new Font("Segoe UI", Font.BOLD, 26));
        startBtn.setForeground(Palette.WELCOME_TEXT); // mint text on deep red background — high contrast
        startBtn.setBorderPainted(false);
        startBtn.setContentAreaFilled(false);
        startBtn.setOpaque(false);
        startBtn.setSize(200, 70);
        startBtn.setFocusPainted(false);
        panel.add(startBtn);

        // open menu
        startBtn.addActionListener(e -> {
            new MainMenuScreen().setVisible(true);
            dispose();
        });

        // RESPONSIVE CENTERING
        addComponentListener(new ComponentAdapter() {
            @Override
            public void componentResized(ComponentEvent e) {
                centerWelcome();
            }
        });

        centerWelcome();
    }

    private void centerWelcome() {
        int w = getWidth();
        int h = getHeight();

        int logoY = (int) (h * 0.18);
        int logoX = (w - logo.getWidth()) / 2;
        logo.setLocation(logoX, logoY);

        int btnX = (w - startBtn.getWidth()) / 2;
        int btnY = logoY + logo.getHeight() + 60;
        startBtn.setLocation(btnX, btnY);
    }

    public static void main(String[] args) {
        // Disable ALL console output
        System.setOut(new java.io.PrintStream(java.io.OutputStream.nullOutputStream()));
        System.setErr(new java.io.PrintStream(java.io.OutputStream.nullOutputStream()));
        SwingUtilities.invokeLater(() -> new CalculusToolkit().setVisible(true));
    }
}

// =================================================================
// COLOR PALLETE
// ================================================================
class Palette {

    // Background gradient for all screens
    public static final Color BG_TOP = new Color(255, 255, 255);
    public static final Color BG_BOTTOM = new Color(245, 245, 245);

    // Welcome / Start screen
    public static final Color WELCOME_ACCENT = new Color(110, 13, 37); // Deep Burgundy
    public static final Color WELCOME_TEXT = new Color(164, 233, 213); // Mint text

    // Main Menu
    public static final Color MENU_ACCENT = new Color(139, 27, 27);
    public static final Color MENU_BUTTON = new Color(255, 200, 87); // Yellow contrast

    // Differentiation Screen
    public static final Color DIFF_ACCENT = new Color(110, 13, 37);
    public static final Color DIFF_BUTTON = new Color(164, 233, 213);

    // Polynomial Tools
    public static final Color POLY_ACCENT = new Color(150, 30, 40);
    public static final Color POLY_BUTTON = new Color(193, 171, 166);

    // Evaluate Screen
    public static final Color EVAL_ACCENT = new Color(120, 20, 30);
    public static final Color EVAL_BUTTON = new Color(164, 233, 213);

    // Utility
    public static final Color ACCENT_GENERIC = new Color(110, 13, 37);
    public static final Color MINT = new Color(164, 233, 213);
}

// ======================================================
// DIFFERENTIATION SCREEN
// ======================================================
class DifferentiationScreen extends JFrame {

    JPanel redPanel;
    JTextField inputField;
    JTextArea outputArea;
    String stepsLog = "";
    private final DiffEngine engine = new DiffEngine();

    // mapping for superscript characters
    private static final java.util.Map<Character, Character> SUP = new java.util.HashMap<>();
    private static final java.util.Map<Character, Character> UNSUP = new java.util.HashMap<>();
    static {
        SUP.put('0', '⁰');
        SUP.put('1', '¹');
        SUP.put('2', '²');
        SUP.put('3', '³');
        SUP.put('4', '⁴');
        SUP.put('5', '⁵');
        SUP.put('6', '⁶');
        SUP.put('7', '⁷');
        SUP.put('8', '⁸');
        SUP.put('9', '⁹');
        SUP.put('-', '⁻');
        SUP.put('+', '⁺');
        for (java.util.Map.Entry<Character, Character> e : SUP.entrySet()) {
            UNSUP.put(e.getValue(), e.getKey());
        }
    }

    public DifferentiationScreen() {

        setTitle("Differentiate Expression");
        setSize(600, 950);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setResizable(true);

        // background
        JPanel bg = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2 = (Graphics2D) g;
                g2.setPaint(new GradientPaint(0, 0, Palette.BG_TOP, 0, getHeight(), new Color(240, 240, 240)));
                g2.fillRect(0, 0, getWidth(), getHeight());
            }
        };
        bg.setLayout(null);
        add(bg);

        // main red panel
        redPanel = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2 = (Graphics2D) g;
                g2.setColor(Palette.DIFF_ACCENT);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 40, 40);
            }
        };
        redPanel.setLayout(null);
        redPanel.setSize(460, 820);
        redPanel.setOpaque(false);
        bg.add(redPanel);

        // center redPanel initially and on resize
        addComponentListener(new ComponentAdapter() {
            @Override
            public void componentResized(ComponentEvent e) {
                int x = (getWidth() - redPanel.getWidth()) / 2;
                int y = (getHeight() - redPanel.getHeight()) / 2;
                redPanel.setLocation(x, y);
            }
        });
        int x = (getWidth() - redPanel.getWidth()) / 2;
        int y = (getHeight() - redPanel.getHeight()) / 2;
        redPanel.setLocation(x, y);

        // Title
        JLabel title = new JLabel("DIFFERENTIATE", SwingConstants.CENTER);
        title.setBounds(40, 20, 380, 40);
        title.setForeground(Color.WHITE);
        title.setFont(new Font("Segoe UI", Font.BOLD, 26));
        redPanel.add(title);

        // TOP BACK BUTTON (says BACK)
        JButton backTop = new JButton("BACK") {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                Color bgC = Palette.MINT;
                if (getModel().isPressed())
                    bgC = bgC.darker();
                else if (getModel().isRollover())
                    bgC = bgC.brighter();
                g2.setColor(bgC);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 14, 14);
                g2.dispose();
                super.paintComponent(g);
            }
        };
        backTop.setBounds(16, 18, 84, 36);
        backTop.setFont(new Font("Segoe UI", Font.BOLD, 15));
        backTop.setForeground(Palette.ACCENT_GENERIC);
        backTop.setBorderPainted(false);
        backTop.setContentAreaFilled(false);
        backTop.setFocusPainted(false);
        backTop.setCursor(new Cursor(Cursor.HAND_CURSOR));
        backTop.setHorizontalAlignment(SwingConstants.CENTER);
        backTop.addActionListener(e -> {
            new MainMenuScreen().setVisible(true);
            dispose();
        });
        redPanel.add(backTop);

        // ===== INPUT FIELD (beautiful) =====
        inputField = new JTextField() {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(Color.WHITE);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 18, 18);
                super.paintComponent(g2);
                g2.dispose();
            }

            @Override
            protected void paintBorder(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(new Color(160, 160, 160));
                g2.setStroke(new BasicStroke(2));
                g2.drawRoundRect(1, 1, getWidth() - 3, getHeight() - 3, 18, 18);
                g2.dispose();
            }
        };
        inputField.setFont(new Font("Segoe UI Semibold", Font.PLAIN, 20));
        inputField.setForeground(Color.GRAY);
        inputField.setBounds(50, 90, 360, 50);
        inputField.setCaretColor(Color.BLACK);
        inputField.setBorder(BorderFactory.createEmptyBorder(10, 12, 10, 12));
        inputField.setText("Enter function e.g. 2x^2 + 3x");

        // placeholder
        inputField.addFocusListener(new FocusAdapter() {
            @Override
            public void focusGained(FocusEvent e) {
                if (inputField.getForeground() == Color.GRAY) {
                    inputField.setForeground(Color.BLACK);
                    inputField.setText("");
                }
            }

            @Override
            public void focusLost(FocusEvent e) {
                if (inputField.getText().trim().isEmpty()) {
                    inputField.setForeground(Color.GRAY);
                    inputField.setText("Enter function e.g. 2x^2 + 3x");
                }
            }
        });

        // ========= Enter behavior: normalize caret exponents -> bracketed form, then
        // render superscript only for exponent part
        // Use regex replacement (safe) to avoid accidentally deleting characters.
        inputField.addActionListener(e -> {
            String t = inputField.getText();
            if (t == null || t.isEmpty())
                return;

            // 1) Normalize caret exponents into ^(digits) form
            // handle cases: ^number or ^(anything) -> ensure bracketed when simple number
            // We'll convert ^number (like ^2 or ^-3) to ^(2) while leaving ^(expr) alone.
            String normalized = t;
            // regex: a caret followed by optional spaces then optional '(' OR sign/digits
            // We'll first replace ^(digits) already bracketed -> keep, then replace ^digits
            // -> ^(digits)
            normalized = normalized.replaceAll("\\^\\s*\\(", "^("); // remove space between ^ and (
            // replace ^<digits> (with optional sign) with ^(digits)
            normalized = normalized.replaceAll("\\^\\s*([+-]?\\d+)", "^($1)");

            // 2) Build display string: replace each occurrence of ^(digits) with
            // superscript characters (only exponent part)
            // Using matcher to avoid touching other characters.
            Pattern p = Pattern.compile("\\^\\(([-+]?\\d+)\\)");
            Matcher m = p.matcher(normalized);
            StringBuffer sb = new StringBuffer();
            while (m.find()) {
                String expo = m.group(1); // e.g., "-3" or "2"
                StringBuilder sup = new StringBuilder();
                for (char ch : expo.toCharArray()) {
                    sup.append(SUP.getOrDefault(ch, ch));
                }
                m.appendReplacement(sb, Matcher.quoteReplacement(sup.toString()));
            }
            m.appendTail(sb);
            // set text to display-only (superscripted exponents), but we keep normalized
            // stored in client property
            inputField.setText(sb.toString());
            // store the raw normalized caret-form in a client property so we can send exact
            // raw to engine later
            inputField.putClientProperty("rawNormalized", normalized);
        });

        redPanel.add(inputField);

        // OUTPUT AREA
        outputArea = new JTextArea();
        outputArea.setEditable(false);
        outputArea.setFont(new Font("Consolas", Font.BOLD, 20));
        outputArea.setLineWrap(true);
        outputArea.setWrapStyleWord(true);
        outputArea.setBackground(new Color(254, 250, 233));
        outputArea.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(Palette.DIFF_BUTTON.darker(), 3),
                BorderFactory.createEmptyBorder(10, 10, 10, 10)));
        JScrollPane scroll = new JScrollPane(outputArea);
        scroll.setBounds(50, 370, 360, 330);
        redPanel.add(scroll);

        // Buttons
        JButton diffBtn = makeButton("Differentiate", 170);
        JButton stepBtn = makeButton("Show Steps", 240);
        JButton clearBtn = makeButton("Clear", 310);

        redPanel.add(diffBtn);
        redPanel.add(stepBtn);
        redPanel.add(clearBtn);

        diffBtn.addActionListener(e -> process());
        stepBtn.addActionListener(e -> showStepsOnly());
        clearBtn.addActionListener(e -> {
            inputField.setForeground(Color.GRAY);
            inputField.setText("Enter function e.g. 2x^2 + 3x");
            inputField.putClientProperty("rawNormalized", null);
            outputArea.setText("");
            stepsLog = "";
        });

        SwingUtilities.invokeLater(() -> inputField.requestFocusInWindow());
    }

    private JButton makeButton(String text, int y) {
        JButton btn = new JButton(text) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g;
                Color bg = Palette.DIFF_BUTTON;
                if (getModel().isRollover())
                    bg = bg.brighter();
                if (getModel().isPressed())
                    bg = bg.darker();
                g2.setColor(bg);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 35, 35);
                super.paintComponent(g);
            }
        };
        btn.setBounds(50, y, 360, 50);
        btn.setFont(new Font("Segoe UI", Font.BOLD, 16));
        btn.setForeground(Palette.ACCENT_GENERIC);
        btn.setBorderPainted(false);
        btn.setContentAreaFilled(false);
        btn.setFocusPainted(false);
        return btn;
    }

    // PROCESS: use rawNormalized (if exists) else take visible text and revert
    // superscripts -> raw caret form
    private void process() {
        try {
            String rawNormalized = (String) inputField.getClientProperty("rawNormalized");
            String raw;
            if (rawNormalized != null && !rawNormalized.trim().isEmpty()) {
                raw = rawNormalized;
            } else {
                // convert displayed superscripts back to caret form
                raw = fromDisplayToRaw(inputField.getText());
            }

            raw = raw.trim();
            if (raw.isEmpty() || inputField.getForeground() == Color.GRAY) {
                JOptionPane.showMessageDialog(this, "Please enter an expression in x");
                return;
            }

            String derivative = engine.differentiate(raw);
            stepsLog = engine.getSteps();
            String cleanOriginal = engine.cleanOriginal(raw);

            // display with superscripts for readability
            String dispOriginal = renderSuperscripts(cleanOriginal);
            String dispDerivative = renderSuperscripts(derivative);

            outputArea.setText("d/dx [" + dispOriginal + "] =\n" + dispDerivative);
            outputArea.setCaretPosition(0);
        } catch (IllegalArgumentException ex) {
            outputArea.setText("❌ Invalid expression: " + ex.getMessage());
        } catch (Exception ex) {
            outputArea.setText("❌ Error while differentiating. Please check your input.");
        }
    }

    // Show steps
  private void showStepsOnly() {
    // ensure we have latest steps (differentiate() should be called first)
    stepsLog = engine.getSteps();
    if (stepsLog == null || stepsLog.isEmpty()) {
        JOptionPane.showMessageDialog(this, "No steps available. First click Differentiate.");
        return;
    }
    // show engine-produced steps exactly (clean, line-by-line)
    outputArea.setText(stepsLog);
    outputArea.setCaretPosition(0);
}


    // Convert raw string containing ^(digits) to display with Unicode superscript
 // Replace existing renderSuperscripts with this (safer)
private String renderSuperscripts(String raw) {
    if (raw == null || raw.isEmpty()) return raw;
    StringBuilder out = new StringBuilder();

    for (int i = 0; i < raw.length(); i++) {
        char c = raw.charAt(i);

        if (c == '^') {
            // case: ^( ... )  -> only convert to superscript if inside is purely digits or signed digits
            if (i + 1 < raw.length() && raw.charAt(i + 1) == '(') {
                int j = raw.indexOf(')', i + 2);
                if (j != -1) {
                    String expo = raw.substring(i + 2, j);
                    // only digits with optional leading +/-
                    if (expo.matches("[+-]?\\d+")) {
                        for (char ch : expo.toCharArray()) out.append(SUP.getOrDefault(ch, ch));
                    } else {
                        // keep caret + parentheses for complex exponent
                        out.append("^(").append(expo).append(")");
                    }
                    i = j;
                    continue;
                }
            }

            // case: simple ^3 or ^-2  -> convert
            if (i + 1 < raw.length()) {
                int j = i + 1;
                StringBuilder expo = new StringBuilder();
                while (j < raw.length() && "+-0123456789".indexOf(raw.charAt(j)) != -1) {
                    expo.append(raw.charAt(j));
                    j++;
                }
                if (expo.length() > 0) {
                    for (char ch : expo.toString().toCharArray())
                        out.append(SUP.getOrDefault(ch, ch));
                    i = j - 1;
                    continue;
                }
            }
        }

        out.append(c);
    }

    return out.toString();
}


    // Convert displayed string (may contain superscript Unicode) back to caret form
    // ^(digits)
    private String fromDisplayToRaw(String s) {
        if (s == null || s.isEmpty())
            return s;
        StringBuilder out = new StringBuilder();
        for (int i = 0; i < s.length();) {
            char c = s.charAt(i);
            if (UNSUP.containsKey(c)) {
                StringBuilder digits = new StringBuilder();
                while (i < s.length() && UNSUP.containsKey(s.charAt(i))) {
                    digits.append(UNSUP.get(s.charAt(i)));
                    i++;
                }
                out.append("^(").append(digits).append(")");
            } else {
                out.append(c);
                i++;
            }
        }
        return out.toString();
    }
}

// ========================================================
// DIFFERENTIATE LOGIC
// ==========================================================

class DiffEngine {

    private final StringBuilder steps = new StringBuilder();

    private static class Node {
        String val;
        Node left, right;
        Node(String v) { val = v; }
        Node(String v, Node l, Node r) { val = v; left = l; right = r; }
    }

    // ---------- predicates ----------
    private boolean isNum(String s) { return s != null && s.matches("-?\\d+(\\.\\d+)?"); }
    private boolean isVar(String s) { return "x".equals(s) || "X".equals(s); }
    private boolean isConst(String s) { return s != null && (s.equals("e") || s.equals("pi") || s.equals("PI")); }

    private boolean isFunc(String s) {
        if (s == null) return false;
        String u = s.toLowerCase();
        return u.equals("sin") || u.equals("cos") || u.equals("tan") || u.equals("cot") ||
               u.equals("sec") || u.equals("csc") ||
               u.equals("asin") || u.equals("acos") || u.equals("atan") ||
               u.equals("arcsin") || u.equals("arccos") || u.equals("arctan") || u.equals("arccot") ||
               u.equals("asec") || u.equals("acsc") ||
               u.equals("ln") || u.equals("log") || u.equals("exp") || u.equals("sqrt") ||
               u.equals("sinh") || u.equals("cosh") || u.equals("tanh") ||
               u.equals("asinh") || u.equals("acosh") || u.equals("atanh");
    }

    // ---------- sanitize ----------
    private String sanitize(String s) {
        if (s == null) return "";
        String t = s;

        // normalize dashes and remove invisible controls
        t = t.replace("–","-").replace("—","-").replace("−","-");
        t = t.replaceAll("[\\p{Cntrl}&&[^\r\n\t]]+","");

        // sqrt symbol -> sqrt
        t = t.replace("\u221A(", "sqrt(");
        t = t.replace("\u221A", "sqrt");

        // superscript unicode -> caret digits
        java.util.Map<Character, Character> supMap = new java.util.HashMap<>();
        supMap.put('⁰','0'); supMap.put('¹','1'); supMap.put('²','2'); supMap.put('³','3'); supMap.put('⁴','4');
        supMap.put('⁵','5'); supMap.put('⁶','6'); supMap.put('⁷','7'); supMap.put('⁸','8'); supMap.put('⁹','9');
        supMap.put('⁻','-'); supMap.put('⁺','+');
        StringBuilder sb = new StringBuilder();
        for (int i=0;i<t.length();++i) {
            char c = t.charAt(i);
            if (supMap.containsKey(c)) {
                if (sb.length()>0 && sb.charAt(sb.length()-1)=='^') sb.append(supMap.get(c));
                else { sb.append("^"); sb.append(supMap.get(c)); }
            } else sb.append(c);
        }
        t = sb.toString();

        // remove whitespace
        t = t.replaceAll("\\s+","");

        // inverse trig variants -> arc* names
        t = t.replaceAll("(?i)sin\\^?\\(?-?1\\)?\\s*\\(", "arcsin(");
        t = t.replaceAll("(?i)cos\\^?\\(?-?1\\)?\\s*\\(", "arccos(");
        t = t.replaceAll("(?i)tan\\^?\\(?-?1\\)?\\s*\\(", "arctan(");
        t = t.replaceAll("(?i)cot\\^?\\(?-?1\\)?\\s*\\(", "arccot(");
        t = t.replaceAll("(?i)sec\\^?\\(?-?1\\)?\\s*\\(", "asec(");
        t = t.replaceAll("(?i)csc\\^?\\(?-?1\\)?\\s*\\(", "acsc(");

        // forms without parentheses: sin^-1x -> arcsin(x)
        t = t.replaceAll("(?i)sin\\^?\\(?-?1\\)?([A-Za-z0-9])","arcsin($1)");
        t = t.replaceAll("(?i)cos\\^?\\(?-?1\\)?([A-Za-z0-9])","arccos($1)");
        t = t.replaceAll("(?i)tan\\^?\\(?-?1\\)?([A-Za-z0-9])","arctan($1)");
        t = t.replaceAll("(?i)cot\\^?\\(?-?1\\)?([A-Za-z0-9])","arccot($1)");
        t = t.replaceAll("(?i)sec\\^?\\(?-?1\\)?([A-Za-z0-9])","asec($1)");
        t = t.replaceAll("(?i)csc\\^?\\(?-?1\\)?([A-Za-z0-9])","acsc($1)");

        // normalize PI
        t = t.replaceAll("(?i)PI","pi");

        // e^... -> exp(...)
        {
            Pattern pe = Pattern.compile("(?i)\\be\\^(\\([^)]*\\)|[A-Za-z0-9\\.\\+\\-]+)");
            Matcher me = pe.matcher(t);
            StringBuffer sb2 = new StringBuffer();
            while (me.find()) {
                String arg = me.group(1);
                if (arg.startsWith("(") && arg.endsWith(")")) arg = arg.substring(1,arg.length()-1);
                me.appendReplacement(sb2, Matcher.quoteReplacement("exp(" + arg + ")"));
            }
            me.appendTail(sb2);
            t = sb2.toString();
        }

        // attach parentheses for functions without them, safely
        String funcList = "sin|cos|tan|cot|sec|csc|ln|log|exp|sqrt|asin|acos|atan|arcsin|arccos|arctan|arccot|asec|acsc|sinh|cosh|tanh|asinh|acosh|atanh";
        Pattern p = Pattern.compile("(?i)\\b(" + funcList + ")\\b(?:(?!\\())([A-Za-z0-9\\.\\^\\+\\-]+)");
        Matcher m = p.matcher(t);
        StringBuffer out = new StringBuffer();
        while (m.find()) {
            String fname = m.group(1).toLowerCase();
            String arg = m.group(2);
            m.appendReplacement(out, Matcher.quoteReplacement(fname + "(" + arg + ")"));
        }
        m.appendTail(out);
        t = out.toString();

        // sqrtx -> sqrt(x)
        t = t.replaceAll("(?i)sqrt([A-Za-z0-9\\.\\^\\+\\-]+)", "sqrt($1)");
        // remove accidental func*( -> func(
        t = t.replaceAll("(?i)\\b(" + funcList + ")\\*\\(", "$1(");

        // unify function names to lowercase
        for (String f : new String[] {"sin","cos","tan","cot","sec","csc","ln","log","exp","sqrt","asin","acos","atan","arcsin","arccos","arctan","arccot","asec","acsc","sinh","cosh","tanh","asinh","acosh","atanh"}) {
            t = t.replaceAll("(?i)\\b"+f+"\\b", f);
        }

        return t.trim();
    }

    // ---------- tokenizer ----------
    private ArrayList<String> tokenize(String s) {
        ArrayList<String> out = new ArrayList<>();
        if (s == null || s.isEmpty()) return out;
        StringBuilder cur = new StringBuilder();
        for (int i=0;i<s.length();++i) {
            char c = s.charAt(i);
            if (Character.isLetter(c)) {
                cur.append(c);
                if (i < s.length()-1 && Character.isLetter(s.charAt(i+1))) continue;
                out.add(cur.toString().toLowerCase());
                cur.setLength(0);
            } else if (Character.isDigit(c) || c=='.') {
                cur.append(c);
                if (i < s.length()-1 && (Character.isDigit(s.charAt(i+1)) || s.charAt(i+1)=='.')) continue;
                out.add(cur.toString());
                cur.setLength(0);
            } else {
                if (cur.length()>0) { out.add(cur.toString()); cur.setLength(0); }
                out.add(String.valueOf(c));
            }
        }
        return out;
    }

    // ---------- unary minus & implicit multiplication ----------
    private ArrayList<String> handleUnaryAndImplicit(ArrayList<String> tokens) {
        ArrayList<String> out = new ArrayList<>();
        for (int i=0;i<tokens.size();++i) {
            String t = tokens.get(i);
            if (t.equals("-")) {
                boolean unary = (i==0) || "(".equals(tokens.get(i-1)) || "+-*/^".contains(tokens.get(i-1));
                if (unary) { out.add("0"); out.add("-"); continue; }
            }
            out.add(t);
            if (i < tokens.size()-1) {
                String b = tokens.get(i+1);
                if (isFunc(t) && (isVar(b) || isNum(b))) {
                    out.remove(out.size()-1);
                    out.add(t); out.add("("); out.add(b); out.add(")");
                    i++; continue;
                }
                if (isFunc(t) && "(".equals(b)) continue;
                boolean left = isNum(t) || isVar(t) || ")".equals(t) || isFunc(t);
                boolean right = isNum(b) || isVar(b) || "(".equals(b) || isFunc(b);
                if (left && right) out.add("*");
            }
        }
        return out;
    }

    // ---------- precedence ----------
    private int prec(String op) {
        if (op == null) return 0;
        if (op.equals("+") || op.equals("-")) return 1;
        if (op.equals("*") || op.equals("/")) return 2;
        if (op.equals("^")) return 3;
        if (isFunc(op)) return 4;
        return 0;
    }

    // ---------- shunting-yard ----------
    private ArrayList<String> infixToPostfix(ArrayList<String> t) {
        Stack<String> st = new Stack<>();
        ArrayList<String> out = new ArrayList<>();
        for (String s : t) {
            if (isNum(s) || isVar(s) || isConst(s)) out.add(s);
            else if (isFunc(s)) st.push(s);
            else if ("(".equals(s)) st.push(s);
            else if (")".equals(s)) {
                while (!st.isEmpty() && !st.peek().equals("(")) out.add(st.pop());
                if (st.isEmpty()) throw new IllegalArgumentException("Mismatched parentheses.");
                st.pop();
                if (!st.isEmpty() && isFunc(st.peek())) out.add(st.pop());
            } else if ("+-*/^".contains(s)) {
                while (!st.isEmpty() && !st.peek().equals("(")) {
                    String top = st.peek();
                    int pTop = prec(top);
                    int pS = prec(s);
                    if (pTop > pS) out.add(st.pop());
                    else if (pTop == pS) {
                        if (s.equals("^")) break; else out.add(st.pop());
                    } else break;
                }
                st.push(s);
            } else out.add(s);
        }
        while (!st.isEmpty()) {
            String top = st.pop();
            if ("(".equals(top) || ")".equals(top)) throw new IllegalArgumentException("Mismatched parentheses.");
            out.add(top);
        }
        return out;
    }

    // ---------- postfix -> tree ----------
    private Node buildTree(ArrayList<String> p) {
        Stack<Node> st = new Stack<>();
        for (String s : p) {
            if (isNum(s) || isVar(s) || isConst(s) || (!isFunc(s) && !"+-*/^".contains(s))) {
                st.push(new Node(s));
            } else if (isFunc(s)) {
                if (st.isEmpty()) throw new IllegalArgumentException("Bad function argument for " + s);
                Node arg = st.pop();
                Node n = new Node(s);
                n.left = arg;
                st.push(n);
            } else if ("+-*/^".contains(s)) {
                if (st.size() < 2) throw new IllegalArgumentException("Invalid operator usage: " + s);
                Node b = st.pop();
                Node a = st.pop();
                st.push(new Node(s, a, b));
            } else throw new IllegalArgumentException("Unexpected token: " + s);
        }
        if (st.size() != 1) throw new IllegalArgumentException("Invalid expression structure.");
        return st.pop();
    }

    // ---------- flatten / factor helpers ----------
    private ArrayList<Node> flattenAdd(Node n) {
        ArrayList<Node> list = new ArrayList<>();
        if (n == null) return list;
        if ("+".equals(n.val)) { list.addAll(flattenAdd(n.left)); list.addAll(flattenAdd(n.right)); }
        else list.add(n);
        return list;
    }
    private void collectMulFactors(Node n, ArrayList<Node> factors) {
        if (n == null) return;
        if ("*".equals(n.val)) { collectMulFactors(n.left,factors); collectMulFactors(n.right,factors); }
        else factors.add(n);
    }

    // ---------- builders ----------
    private Node add(Node a, Node b) { return new Node("+", a, b); }
    private Node sub(Node a, Node b) { return new Node("-", a, b); }
    private Node mul(Node a, Node b) { return new Node("*", a, b); }
    private Node div(Node a, Node b) { return new Node("/", a, b); }
    private Node pow(Node a, Node b) { return new Node("^", a, b); }
    private Node func(String name, Node u) { Node n = new Node(name); n.left = u; return n; }

    // ---------- differentiation ----------
    private Node diff(Node n) {
        if (n == null) return null;
        if (isNum(n.val) || isConst(n.val)) return new Node("0");
        if (isVar(n.val)) return new Node("1");

        if (isFunc(n.val)) {
            Node u = n.left;
            Node du = diff(u);
            switch (n.val) {
                case "sin": return mul(func("cos", u), du);
                case "cos": return mul(new Node("-1"), mul(func("sin", u), du));
                case "tan": return mul(pow(func("sec", u), new Node("2")), du);
                case "cot": return mul(new Node("-1"), mul(pow(func("csc", u), new Node("2")), du));
                case "sec": return mul(mul(func("sec", u), func("tan", u)), du);
                case "csc": return mul(new Node("-1"), mul(mul(func("csc", u), func("cot", u)), du));
                case "ln": return div(du, u);
                case "log": return div(du, u);
                case "exp": return mul(func("exp", u), du);
                case "sqrt": return div(du, mul(new Node("2"), func("sqrt", u)));
                case "asin": case "arcsin":
                    return div(du, func("sqrt", sub(new Node("1"), pow(u, new Node("2")))));
                case "acos": case "arccos":
                    return mul(new Node("-1"), div(du, func("sqrt", sub(new Node("1"), pow(u, new Node("2"))))));
                case "atan": case "arctan":
                    return div(du, add(new Node("1"), pow(u, new Node("2"))));
                case "arccot":
                    return mul(new Node("-1"), div(du, add(new Node("1"), pow(u, new Node("2")))));
                case "asec":
                    return div(du, mul(u, func("sqrt", sub(pow(u,new Node("2")), new Node("1")))));
                case "acsc":
                    return mul(new Node("-1"), div(du, mul(u, func("sqrt", sub(pow(u,new Node("2")), new Node("1"))))));
                case "sinh": return mul(func("cosh", u), du);
                case "cosh": return mul(func("sinh", u), du);
                case "tanh": return mul(sub(new Node("1"), pow(func("tanh", u), new Node("2"))), du);
                case "asinh":
                    return div(du, func("sqrt", add(pow(u,new Node("2")), new Node("1"))));
                case "acosh":
                    return div(du, mul(func("sqrt", sub(u, new Node("1"))), func("sqrt", add(u, new Node("1")))));
                case "atanh":
                    return div(du, sub(new Node("1"), pow(u, new Node("2"))));
            }
        }

        switch (n.val) {
            case "+": return add(diff(n.left), diff(n.right));
            case "-": return sub(diff(n.left), diff(n.right));
            case "*": return add(mul(diff(n.left), n.right), mul(n.left, diff(n.right)));
            case "/": return div(sub(mul(diff(n.left), n.right), mul(n.left, diff(n.right))), pow(n.right, new Node("2")));
            case "^": {
                Node u = n.left, v = n.right;
                if (v != null && isNum(v.val)) {
                    double k = Double.parseDouble(v.val);
                    return mul(new Node(trimDouble(k)), mul(pow(u, new Node(trimDouble(k - 1))), diff(u)));
                } else if (u != null && isNum(u.val) && u.val.equals("e")) {
                    return mul(pow(u, v), mul(diff(v), func("ln", u)));
                } else {
                    Node term1 = mul(diff(v), func("ln", u));
                    Node term2 = mul(v, div(diff(u), u));
                    return mul(pow(u, v), add(term1, term2));
                }
            }
        }

        return new Node("0");
    }

    // ---------- simplifier ----------
    private Node simplify(Node n) {
        if (n == null) return null;
        n.left = simplify(n.left);
        n.right = simplify(n.right);

        if (n.left==null && n.right==null) {
            if (isConst(n.val)) {
                if ("e".equals(n.val)) return new Node(trimDouble(Math.E));
                if ("pi".equals(n.val)) return new Node(trimDouble(Math.PI));
            }
            return n;
        }

        if (n.left!=null && n.right!=null && isNum(n.left.val) && isNum(n.right.val)) {
            double a = Double.parseDouble(n.left.val);
            double b = Double.parseDouble(n.right.val);
            switch (n.val) {
                case "+": return new Node(trimDouble(a+b));
                case "-": return new Node(trimDouble(a-b));
                case "*": return new Node(trimDouble(a*b));
                case "/": return new Node(trimDouble(a/b));
                case "^": return new Node(trimDouble(Math.pow(a,b)));
            }
        }

        if ("+".equals(n.val)) {
            if (isNum(n.left.val) && n.left.val.equals("0")) return n.right;
            if (isNum(n.right.val) && n.right.val.equals("0")) return n.left;
            return n;
        }
        if ("-".equals(n.val)) {
            if (isNum(n.right.val) && n.right.val.equals("0")) return n.left;
            return n;
        }
        if ("*".equals(n.val)) {
            ArrayList<Node> factors = new ArrayList<>();
            collectMulFactors(n, factors);
            double numeric = 1.0;
            ArrayList<Node> others = new ArrayList<>();
            int totalX = 0;
            for (Node f : factors) {
                if (isNum(f.val)) numeric *= Double.parseDouble(f.val);
                else if (isConst(f.val)) {
                    if ("e".equals(f.val)) numeric *= Math.E;
                    else if ("pi".equals(f.val)) numeric *= Math.PI;
                } else if (isVar(f.val)) totalX += 1;
                else if ("^".equals(f.val) && f.left!=null && isVar(f.left.val) && f.right!=null && isNum(f.right.val))
                    totalX += (int)Math.round(Double.parseDouble(f.right.val));
                else others.add(f);
            }
            if (Math.abs(numeric) < 1e-12) return new Node("0");
            Node res = null;
            if (totalX != 0) {
                String xp = totalX==1 ? "x" : ("x^"+totalX);
                if (Math.abs(numeric-1.0)>1e-12) {
                    String ns = trimDouble(numeric);
                    if ("-1".equals(ns)) res = new Node("-"+xp);
                    else res = new Node("*", new Node(ns), new Node(xp));
                } else res = new Node(xp);
            } else {
                if (Math.abs(numeric-1.0)>1e-12) res = new Node(trimDouble(numeric));
            }
            for (Node f : others) {
                if (res==null) res = f;
                else res = new Node("*", res, f);
            }
            if (res==null) return new Node("1");
            return res;
        }
        if ("/".equals(n.val)) {
            if (isNum(n.left.val) && n.left.val.equals("0")) return new Node("0");
            if (isNum(n.right.val) && n.right.val.equals("1")) return n.left;
            return n;
        }
        if ("^".equals(n.val)) {
            if (isNum(n.right.val) && n.right.val.equals("1")) return n.left;
            if (isNum(n.right.val) && n.right.val.equals("0")) return new Node("1");
            if (n.left!=null && "^".equals(n.left.val) && n.left.right!=null && isNum(n.left.right.val) && isNum(n.right.val)) {
                double a = Double.parseDouble(n.left.right.val);
                double b = Double.parseDouble(n.right.val);
                return new Node("^", n.left.left, new Node(trimDouble(a*b)));
            }
            return n;
        }
        return n;
    }

    private String trimDouble(double v) {
        if (Double.isNaN(v) || Double.isInfinite(v)) return String.valueOf(v);
        if (Math.abs(v - Math.round(v)) < 1e-12) return String.valueOf((long)Math.round(v));
        String s = String.valueOf(v);
        if (s.contains("E")) return s;
        if (s.indexOf('.') >= 0) {
            while (s.endsWith("0")) s = s.substring(0, s.length()-1);
            if (s.endsWith(".")) s = s.substring(0, s.length()-1);
        }
        return s;
    }

    // ---------- monomial detection ----------
    private double[] getMonomialCoeffPower(Node n) {
        if (n==null) return null;
        if (isVar(n.val)) return new double[]{1.0,1.0};
        if ("^".equals(n.val) && n.left!=null && isVar(n.left.val) && n.right!=null && isNum(n.right.val))
            return new double[]{1.0, Double.parseDouble(n.right.val)};
        if ("*".equals(n.val)) {
            if (n.left!=null && isNum(n.left.val)) {
                double c = Double.parseDouble(n.left.val);
                if (n.right!=null && isVar(n.right.val)) return new double[]{c,1.0};
                if (n.right!=null && "^".equals(n.right.val) && n.right.left!=null && isVar(n.right.left.val) && n.right.right!=null && isNum(n.right.right.val))
                    return new double[]{c, Double.parseDouble(n.right.right.val)};
            }
            if (n.right!=null && isNum(n.right.val)) {
                double c = Double.parseDouble(n.right.val);
                if (n.left!=null && isVar(n.left.val)) return new double[]{c,1.0};
                if (n.left!=null && "^".equals(n.left.val) && n.left.left!=null && isVar(n.left.left.val) && n.left.right!=null && isNum(n.left.right.val))
                    return new double[]{c, Double.parseDouble(n.left.right.val)};
            }
        }
        return null;
    }

    private String formatMonomial(Node term, double coeff, int pow) {
        String cstr = trimDouble(coeff);
        if (pow==0) return cstr;
        if (pow==1) {
            if (Math.abs(coeff-1.0)<1e-12) return "x";
            if (Math.abs(coeff+1.0)<1e-12) return "-x";
            return cstr + "x";
        } else {
            if (Math.abs(coeff-1.0)<1e-12) return "x^"+pow;
            if (Math.abs(coeff+1.0)<1e-12) return "-x^"+pow;
            return cstr + "x^"+pow;
        }
    }

    private String stripPlus(String s) {
        if (s==null) return null;
        if (s.startsWith("+")) return s.substring(1);
        return s;
    }

    // ---------- pretty print ----------
    public String print(Node n) {
        if (n==null) return "";
        if (n.left==null && n.right==null) return n.val;

        if (isFunc(n.val)) {
            String arg = n.left != null ? print(n.left) : "";
            return n.val + "(" + arg + ")";
        }

        String L = n.left != null ? print(n.left) : "";
        String R = n.right != null ? print(n.right) : "";

        switch (n.val) {
            case "+": if (R.startsWith("-")) return L + " - " + R.substring(1); return L + " + " + R;
            case "-": if (R.startsWith("-")) return L + " + " + R.substring(1); return L + " - (" + R + ")";
            case "*":
                if (isNum(L) && (R.equals("x") || R.startsWith("x^"))) {
                    String num = stripPlus(L);
                    if ("1".equals(num)) return R;
                    if ("-1".equals(num)) return "-" + R;
                    return num + R;
                }
                if (isNum(R) && (L.equals("x") || L.startsWith("x^"))) {
                    String num = stripPlus(R);
                    if ("1".equals(num)) return L;
                    if ("-1".equals(num)) return "-" + L;
                    return num + L;
                }
                if (isNum(L) && isNum(R)) return stripPlus(L) + "*" + stripPlus(R);
                return L + "*" + R;
            case "/": return "(" + L + ")/(" + R + ")";
            case "^":
                if (isNum(R) || isVar(R)) return L + "^" + R;
                if (R.contains("^") || R.matches(".*[+\\-*/].*")) return L + "^(" + R + ")";
                return L + "^" + R;
            default:
                if (!L.isEmpty() && !R.isEmpty()) return L + " " + n.val + " " + R;
                if (!L.isEmpty()) return n.val + "(" + L + ")";
                return n.val;
        }
    }

    // convert exp(x) -> e^(x) back for UI (keeps parentheses)
    private String renderExpBack(String s) {
        if (s==null || s.isEmpty()) return s;
        Pattern p = Pattern.compile("exp\\(([^()]+)\\)");
        Matcher m = p.matcher(s);
        StringBuffer sb = new StringBuffer();
        while (m.find()) {
            String arg = m.group(1).trim();
            m.appendReplacement(sb, "e^(" + arg + ")");
        }
        m.appendTail(sb);
        return sb.toString();
    }

    public String differentiate(String expr) {
        steps.setLength(0);
        String cleaned = sanitize(expr);
        ArrayList<String> tokens = tokenize(cleaned);
        tokens = handleUnaryAndImplicit(tokens);
        ArrayList<String> post = infixToPostfix(tokens);

        // clear + debug header
        steps.append("f(x) = ").append(cleaned).append("\n\n");
    
        Node tree = buildTree(post);

        ArrayList<Node> terms = flattenAdd(tree);
        ArrayList<Node> simplifiedTerms = new ArrayList<>();

        for (Node term : terms) {
            String termStr = print(term);
            double[] mon = getMonomialCoeffPower(term);
            if (mon != null) {
                double coeff = mon[0]; int power = (int)Math.round(mon[1]);
                double newCoeff = coeff * power;
                String simp;
                if (power - 1 == 0) simp = trimDouble(newCoeff);
                else if (power - 1 == 1) simp = trimDouble(newCoeff) + "x";
                else simp = trimDouble(newCoeff) + "x^" + (power - 1);

                steps.append("d/dx(").append(formatMonomial(term, coeff, power)).append(") = ")
                     .append(trimDouble(coeff)).append(" * ").append(power).append(" * x^").append(power-1)
                     .append("  →  ").append(simp).append("\n");

                Node dNode;
                if (power-1==0) dNode = new Node(trimDouble(newCoeff));
                else if (power-1==1) {
                    if (Math.abs(newCoeff-1.0)<1e-12) dNode = new Node("x");
                    else if (Math.abs(newCoeff+1.0)<1e-12) dNode = new Node("-x");
                    else dNode = new Node("*", new Node(trimDouble(newCoeff)), new Node("x"));
                } else {
                    Node xpow = new Node("^", new Node("x"), new Node(String.valueOf(power-1)));
                    if (Math.abs(newCoeff-1.0)<1e-12) dNode = xpow;
                    else if (Math.abs(newCoeff+1.0)<1e-12) dNode = new Node("*", new Node("-1"), xpow);
                    else dNode = new Node("*", new Node(trimDouble(newCoeff)), xpow);
                }
                simplifiedTerms.add(dNode);
                continue;
            }

            Node dRaw = diff(term);
            Node dSimp = simplify(dRaw);
            dSimp = simplify(dSimp);
            String dRawStr = print(dRaw);
            String dSimpStr = print(dSimp);
            steps.append("d/dx(").append(termStr).append(") = ").append(dRawStr);
            if (!dRawStr.equals(dSimpStr)) steps.append("  →  ").append(dSimpStr);
            steps.append("\n");
            simplifiedTerms.add(dSimp);
        }

        Node combined = null;
        for (Node t : simplifiedTerms) {
            if (combined==null) combined = t;
            else combined = add(combined, t);
        }
        if (combined==null) combined = new Node("0");

        Node finalSimp = simplify(combined);
        finalSimp = simplify(finalSimp);

        String finalResult = print(finalSimp);
        finalResult = renderExpBack(finalResult);

        // summary lines (clean)
        StringBuilder combinedBefore = new StringBuilder();
        for (int i=0;i<simplifiedTerms.size();++i) {
            String s = print(simplifiedTerms.get(i));
            if (i>0 && !s.startsWith("-")) combinedBefore.append(" + ");
            combinedBefore.append(s);
        }
        if (combinedBefore.length()==0) combinedBefore.append("0");

        steps.append("\nCombined derivative (before simplify): ").append(combinedBefore.toString()).append("\n");
        steps.append("Derivative (simplified): ").append(finalResult).append("\n");

        return finalResult;
    }

    public String getSteps() { return steps.toString(); }
    public String cleanOriginal(String raw) { return sanitize(raw); }
}


// =======================================================
// POLYNOMIALTOOL SCREEN
// =======================================================
class PolynomialToolsScreen extends JFrame {

    JButton addBtn, subBtn, mulBtn, divBtn, backBtn;

    public PolynomialToolsScreen() {

        setTitle("Polynomial Tools");
        setSize(550, 900);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setResizable(true);

        JPanel mainPanel = new JPanel() {
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2 = (Graphics2D) g;
                g2.setPaint(new GradientPaint(0, 0, Palette.BG_TOP, 0, getHeight(), Palette.BG_BOTTOM));
                g2.fillRect(0, 0, getWidth(), getHeight());
            }
        };

        mainPanel.setLayout(null);
        add(mainPanel);

        JPanel redPanel = new JPanel() {
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2 = (Graphics2D) g;
                g2.setColor(Palette.POLY_ACCENT);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 40, 40);
            }
        };

        redPanel.setLayout(null);
        redPanel.setSize(400, 550);
        redPanel.setOpaque(false);
        mainPanel.add(redPanel);

        redPanel.setLocation(
                (getWidth() - redPanel.getWidth()) / 2,
                (getHeight() - redPanel.getHeight()) / 2 - 80);

        JLabel title = new JLabel("POLYNOMIAL TOOLS", SwingConstants.CENTER);
        title.setFont(new Font("Segoe UI", Font.BOLD, 26));
        title.setForeground(Color.WHITE);
        title.setBounds(25, 25, 350, 40);
        redPanel.add(title);

        addBtn = createButton("Addition", 150);
        subBtn = createButton("Subtraction", 230);
        mulBtn = createButton("Multiplication", 310);
        divBtn = createButton("Division", 390);
        backBtn = createButton("Back", 470);

        redPanel.add(addBtn);
        redPanel.add(subBtn);
        redPanel.add(mulBtn);
        redPanel.add(divBtn);
        redPanel.add(backBtn);

        addBtn.addActionListener(e -> new PolynomialOperationScreen("ADD").setVisible(true));
        subBtn.addActionListener(e -> new PolynomialOperationScreen("SUB").setVisible(true));
        mulBtn.addActionListener(e -> new PolynomialOperationScreen("MUL").setVisible(true));
        divBtn.addActionListener(e -> new PolynomialOperationScreen("DIV").setVisible(true));

        backBtn.addActionListener(e -> {
            new MainMenuScreen().setVisible(true);
            dispose();
        });
    }

    private JButton createButton(String text, int y) {
        JButton btn = new JButton(text) {
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g;
                g2.setColor(Palette.POLY_BUTTON);
                if (getModel().isRollover())
                    g2.setColor(Palette.POLY_BUTTON.brighter());
                if (getModel().isPressed())
                    g2.setColor(Palette.POLY_BUTTON.darker());
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 35, 35);
                super.paintComponent(g);
            }
        };

        btn.setFont(new Font("Segoe UI", Font.BOLD, 18));
        btn.setForeground(Palette.ACCENT_GENERIC);
        btn.setBorderPainted(false);
        btn.setContentAreaFilled(false);
        btn.setOpaque(false);
        btn.setBounds(50, y, 300, 55);
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        return btn;
    }
}

class PolynomialOperationScreen extends JFrame {

    JPanel redPanel;
    JTextField p1, p2;
    JTextArea result;
    String type;
    String stepsLog = "";

    // local superscript maps (self-contained)
    private static final java.util.Map<Character, Character> SUP = new java.util.HashMap<>();
    private static final java.util.Map<Character, Character> UNSUP = new java.util.HashMap<>();
    static {
        SUP.put('0', '⁰');
        SUP.put('1', '¹');
        SUP.put('2', '²');
        SUP.put('3', '³');
        SUP.put('4', '⁴');
        SUP.put('5', '⁵');
        SUP.put('6', '⁶');
        SUP.put('7', '⁷');
        SUP.put('8', '⁸');
        SUP.put('9', '⁹');
        SUP.put('-', '⁻');
        SUP.put('+', '⁺');
        for (java.util.Map.Entry<Character, Character> e : SUP.entrySet()) {
            UNSUP.put(e.getValue(), e.getKey());
        }
    }

    public PolynomialOperationScreen(String type) {
        this.type = type;

        setTitle("Polynomial " + type);
        setSize(600, 950);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setResizable(true);

        JPanel bg = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2 = (Graphics2D) g;
                g2.setPaint(new GradientPaint(0, 0, Palette.BG_TOP, 0, getHeight(), Palette.BG_BOTTOM));
                g2.fillRect(0, 0, getWidth(), getHeight());
            }
        };
        bg.setLayout(null);
        add(bg);

        redPanel = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2 = (Graphics2D) g;
                g2.setColor(Palette.POLY_ACCENT);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 40, 40);
            }
        };
        redPanel.setLayout(null);
        redPanel.setSize(460, 750);
        redPanel.setOpaque(false);
        bg.add(redPanel);

        addComponentListener(new ComponentAdapter() {
            @Override
            public void componentResized(ComponentEvent e) {
                redPanel.setLocation((getWidth() - redPanel.getWidth()) / 2,
                        (getHeight() - redPanel.getHeight()) / 2);
            }
        });
        redPanel.setLocation((getWidth() - redPanel.getWidth()) / 2,
                (getHeight() - redPanel.getHeight()) / 2);

        JLabel title = new JLabel("POLYNOMIAL " + type, SwingConstants.CENTER);
        title.setBounds(40, 20, 380, 40);
        title.setForeground(Color.WHITE);
        title.setFont(new Font("Segoe UI", Font.BOLD, 26));
        redPanel.add(title);

        // inputs using same styled createInputField (placeholder + Enter normalization)
        p1 = createInputField("Enter polynomial e.g. 2x^2 + 3x");
        p1.setBounds(50, 90, 360, 50);
        redPanel.add(p1);

        p2 = createInputField("Enter polynomial e.g. x^2 - x");
        p2.setBounds(50, 160, 360, 50);
        redPanel.add(p2);

        JButton calcBtn = makeButton("Calculate", 240);
        JButton backBtn = makeButton("Back", 310);

        redPanel.add(calcBtn);
        redPanel.add(backBtn);

        // RESULT area — made identical style to DifferentiationScreen outputArea
        result = new JTextArea();
        result.setEditable(false);
        result.setFont(new Font("Consolas", Font.BOLD, 20)); // MATCHED FONT
        result.setLineWrap(true);
        result.setWrapStyleWord(true);
        result.setBackground(new Color(254, 250, 233)); // same background
        result.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(Palette.DIFF_BUTTON.darker(), 3),
                BorderFactory.createEmptyBorder(10, 10, 10, 10)));

        JScrollPane scroll = new JScrollPane(result);
        scroll.setBounds(50, 360, 360, 330);
        redPanel.add(scroll);

        calcBtn.addActionListener(e -> calculate());
        backBtn.addActionListener(e -> {
            new PolynomialToolsScreen().setVisible(true);
            dispose();
        });

        SwingUtilities.invokeLater(() -> p1.requestFocusInWindow());
    }

    private JTextField createInputField(String placeholder) {
        JTextField f = new JTextField() {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(Color.WHITE);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 18, 18);
                super.paintComponent(g2);
                g2.dispose();
            }

            @Override
            protected void paintBorder(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(new Color(160, 160, 160));
                g2.setStroke(new BasicStroke(2));
                g2.drawRoundRect(1, 1, getWidth() - 3, getHeight() - 3, 18, 18);
                g2.dispose();
            }
        };
        f.setFont(new Font("Segoe UI Semibold", Font.PLAIN, 20));
        f.setForeground(Color.GRAY);
        f.setCaretColor(Color.BLACK);
        f.setBorder(BorderFactory.createEmptyBorder(10, 12, 10, 12));
        f.setText(placeholder);

        f.addFocusListener(new FocusAdapter() {
            @Override
            public void focusGained(FocusEvent e) {
                if (Color.GRAY.equals(f.getForeground())) {
                    f.setForeground(Color.BLACK);
                    f.setText("");
                }
            }

            @Override
            public void focusLost(FocusEvent e) {
                if (f.getText().trim().isEmpty()) {
                    f.setForeground(Color.GRAY);
                    f.setText(placeholder);
                    f.putClientProperty("rawNormalized", null);
                }
            }
        });

        f.addActionListener(e -> {
            String t = f.getText();
            if (t == null || t.isEmpty())
                return;
            String normalized = t;
            normalized = normalized.replaceAll("\\^\\s*\\(", "^(");
            normalized = normalized.replaceAll("\\^\\s*([+-]?\\d+)", "^($1)");

            Pattern p = Pattern.compile("\\^\\(([-+]?\\d+)\\)");
            Matcher m = p.matcher(normalized);
            StringBuffer sb = new StringBuffer();
            while (m.find()) {
                String expo = m.group(1);
                StringBuilder sup = new StringBuilder();
                for (char ch : expo.toCharArray()) {
                    sup.append(SUP.getOrDefault(ch, ch));
                }
                m.appendReplacement(sb, Matcher.quoteReplacement(sup.toString()));
            }
            m.appendTail(sb);

            f.setText(sb.toString());
            f.putClientProperty("rawNormalized", normalized);
        });

        return f;
    }

    private JButton makeButton(String text, int y) {
        JButton btn = new JButton(text) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g;
                Color bg = Palette.MINT;
                if (getModel().isRollover())
                    bg = bg.brighter();
                if (getModel().isPressed())
                    bg = bg.darker();
                g2.setColor(bg);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 35, 35);
                super.paintComponent(g);
            }
        };
        btn.setBounds(50, y, 360, 50);
        btn.setFont(new Font("Segoe UI", Font.BOLD, 16));
        btn.setForeground(Palette.ACCENT_GENERIC);
        btn.setBorderPainted(false);
        btn.setContentAreaFilled(false);
        btn.setFocusPainted(false);
        return btn;
    }

    private void calculate() {
        try {
            String rawA = (String) p1.getClientProperty("rawNormalized");
            String rawB = (String) p2.getClientProperty("rawNormalized");

            String A = (rawA != null && !rawA.trim().isEmpty()) ? rawA : fromDisplayToRaw(p1.getText());
            String B = (rawB != null && !rawB.trim().isEmpty()) ? rawB : fromDisplayToRaw(p2.getText());

            if (A == null)
                A = "";
            if (B == null)
                B = "";

            // Normalize exponent formatting and spacing
            A = A.replaceAll("\\^\\s*\\(", "^(")
                    .replaceAll("\\(\\s+", "(")
                    .replaceAll("\\s+\\)", ")")
                    .replaceAll("\\s+", " ")
                    .trim();

            B = B.replaceAll("\\^\\s*\\(", "^(")
                    .replaceAll("\\(\\s+", "(")
                    .replaceAll("\\s+\\)", ")")
                    .replaceAll("\\s+", " ")
                    .trim();

            // Field-empty check
            if (A.isEmpty() || B.isEmpty() ||
                    Color.GRAY.equals(p1.getForeground()) ||
                    Color.GRAY.equals(p2.getForeground())) {
                result.setText("Please enter both polynomials.");
                return;
            }

            // Parse
            Polynomial polyA = Polynomial.parse(A);
            Polynomial polyB = Polynomial.parse(B);

            // ---------- FIXED switch (statement, not expression) ----------
            Polynomial ans;

            switch (type) {
                case "ADD":
                    ans = polyA.add(polyB);
                    break;

                case "SUB":
                    ans = polyA.sub(polyB);
                    break;

                case "MUL":
                    ans = polyA.mul(polyB);
                    break;

                case "DIV":
                    try {
                        ans = polyA.div(polyB);
                    } catch (RuntimeException rex) {
                        result.setText("Division error: divisor must be a single-term (monomial).");
                        return; // Allowed now because switch is not an expression
                    }
                    break;

                default:
                    result.setText("Unsupported operation.");
                    return;
            }

            // Simplify and display
            ans = ans.simplify();
            String out = renderSuperscripts(ans.toString());
            result.setText(out);

        } catch (NumberFormatException nfe) {
            result.setText("Invalid polynomial format. Check your coefficients.");

        } catch (Exception ex) {
            result.setText("❌ Error: " +
                    (ex.getMessage() != null ? ex.getMessage() : "Unexpected error."));
        }
    }

    // Convert displayed string (may contain superscript Unicode) back to caret form
    // ^(digits)
    private String fromDisplayToRaw(String s) {
        if (s == null || s.isEmpty())
            return s;
        StringBuilder out = new StringBuilder();
        for (int i = 0; i < s.length();) {
            char c = s.charAt(i);
            if (UNSUP.containsKey(c)) {
                StringBuilder digits = new StringBuilder();
                while (i < s.length() && UNSUP.containsKey(s.charAt(i))) {
                    digits.append(UNSUP.get(s.charAt(i)));
                    i++;
                }
                out.append("^(").append(digits).append(")");
            } else {
                out.append(c);
                i++;
            }
        }
        return out.toString();
    }

    // Replace existing renderSuperscripts with this (safer)
private String renderSuperscripts(String raw) {
    if (raw == null || raw.isEmpty()) return raw;
    StringBuilder out = new StringBuilder();

    for (int i = 0; i < raw.length(); i++) {
        char c = raw.charAt(i);

        if (c == '^') {
            // case: ^( ... )  -> only convert to superscript if inside is purely digits or signed digits
            if (i + 1 < raw.length() && raw.charAt(i + 1) == '(') {
                int j = raw.indexOf(')', i + 2);
                if (j != -1) {
                    String expo = raw.substring(i + 2, j);
                    // only digits with optional leading +/-
                    if (expo.matches("[+-]?\\d+")) {
                        for (char ch : expo.toCharArray()) out.append(SUP.getOrDefault(ch, ch));
                    } else {
                        // keep caret + parentheses for complex exponent
                        out.append("^(").append(expo).append(")");
                    }
                    i = j;
                    continue;
                }
            }

            // case: simple ^3 or ^-2  -> convert
            if (i + 1 < raw.length()) {
                int j = i + 1;
                StringBuilder expo = new StringBuilder();
                while (j < raw.length() && "+-0123456789".indexOf(raw.charAt(j)) != -1) {
                    expo.append(raw.charAt(j));
                    j++;
                }
                if (expo.length() > 0) {
                    for (char ch : expo.toString().toCharArray())
                        out.append(SUP.getOrDefault(ch, ch));
                    i = j - 1;
                    continue;
                }
            }
        }

        out.append(c);
    }

    return out.toString();
}

}

class Polynomial {

    // store terms as map: power -> coefficient (double for safety)
    private final java.util.Map<Integer, Double> terms = new java.util.HashMap<>();

    // optional remainder string for display after division
    private String remainderDisplay = null;

    public Polynomial() {
    }

    public Polynomial(double coeff, int power) {
        if (Math.abs(coeff) > 1e-12)
            terms.put(power, coeff);
    }

    // ----------- Parsing -----------
    public static Polynomial parse(String s) {
        Polynomial p = new Polynomial();
        if (s == null)
            return p;

        s = s.replace("–", "-").replace("—", "-").replace("−", "-");
        s = s.replaceAll("\\s+", "");

        s = convertSuperscriptsToCaret(s);

        if (s.isEmpty())
            return p;

        Pattern termPattern = Pattern.compile("([+-]?\\d*)x(?:\\^\\(?(-?\\d+)\\)?)?|([+-]?\\d+)");
        Matcher m = termPattern.matcher(s);

        while (m.find()) {
            if (m.group(3) != null) {
                // constant
                try {
                    double coeff = Double.parseDouble(m.group(3));
                    p.addTerm(0, coeff);
                } catch (NumberFormatException ex) {
                    // ignore malformed constant
                }
            } else {
                String coeffStr = m.group(1);
                int coeffInt = 1;
                if (coeffStr == null || coeffStr.isEmpty() || coeffStr.equals("+"))
                    coeffInt = 1;
                else if (coeffStr.equals("-"))
                    coeffInt = -1;
                else {
                    try {
                        coeffInt = Integer.parseInt(coeffStr);
                    } catch (NumberFormatException ex) {
                        continue;
                    }
                }
                String powStr = m.group(2);
                int pow = 1;
                if (powStr != null && !powStr.isEmpty()) {
                    try {
                        pow = Integer.parseInt(powStr);
                    } catch (NumberFormatException ex) {
                        pow = 1;
                    }
                }
                p.addTerm(pow, coeffInt);
            }
        }
        return p;
    }

    private static String convertSuperscriptsToCaret(String s) {
        if (s == null || s.isEmpty())
            return s;
        java.util.Map<Character, Character> SUP_MAP = new java.util.HashMap<>();
        SUP_MAP.put('⁰', '0');
        SUP_MAP.put('¹', '1');
        SUP_MAP.put('²', '2');
        SUP_MAP.put('³', '3');
        SUP_MAP.put('⁴', '4');
        SUP_MAP.put('⁵', '5');
        SUP_MAP.put('⁶', '6');
        SUP_MAP.put('⁷', '7');
        SUP_MAP.put('⁸', '8');
        SUP_MAP.put('⁹', '9');
        SUP_MAP.put('⁻', '-');
        SUP_MAP.put('⁺', '+');

        StringBuilder out = new StringBuilder();
        for (int i = 0; i < s.length(); ++i) {
            char c = s.charAt(i);
            if (SUP_MAP.containsKey(c)) {
                if (out.length() > 0 && out.charAt(out.length() - 1) == '^') {
                    out.append(SUP_MAP.get(c));
                } else {
                    out.append('^').append(SUP_MAP.get(c));
                }
            } else {
                out.append(c);
            }
        }
        return out.toString();
    }

    private void addTerm(int power, double coeff) {
        if (Math.abs(coeff) < 1e-12)
            return;
        terms.put(power, terms.getOrDefault(power, 0.0) + coeff);
        if (Math.abs(terms.getOrDefault(power, 0.0)) < 1e-12)
            terms.remove(power);
    }

    // ---------- Operations ----------
    public Polynomial add(Polynomial b) {
        Polynomial r = new Polynomial();
        for (var e : this.terms.entrySet())
            r.addTerm(e.getKey(), e.getValue());
        for (var e : b.terms.entrySet())
            r.addTerm(e.getKey(), e.getValue());
        return r;
    }

    public Polynomial sub(Polynomial b) {
        Polynomial r = new Polynomial();
        for (var e : this.terms.entrySet())
            r.addTerm(e.getKey(), e.getValue());
        for (var e : b.terms.entrySet())
            r.addTerm(e.getKey(), -e.getValue());
        return r;
    }

    public Polynomial mul(Polynomial b) {
        Polynomial r = new Polynomial();
        for (var a : this.terms.entrySet()) {
            for (var bb : b.terms.entrySet()) {
                int pow = a.getKey() + bb.getKey();
                double coeff = a.getValue() * bb.getValue();
                r.addTerm(pow, coeff);
            }
        }
        return r;
    }

    // ---------- Long division: returns quotient; if remainder exists,
    // quotient.remainderDisplay is set ----------
    public Polynomial div(Polynomial divisor) {
        // clear any previous remainder display
        this.remainderDisplay = null;

        if (divisor == null || divisor.terms.isEmpty())
            throw new RuntimeException("Division by zero polynomial.");

        // clone dividend and divisor maps into descending-degree ordered maps
        java.util.TreeMap<Integer, Double> A = new java.util.TreeMap<>((a, b) -> b - a);
        java.util.TreeMap<Integer, Double> B = new java.util.TreeMap<>((a, b) -> b - a);

        A.putAll(this.terms);
        B.putAll(divisor.terms);

        Polynomial quotient = new Polynomial();

        // leading-term long-division
        while (!A.isEmpty() && !B.isEmpty()) {
            int degA = A.firstKey();
            double coeffA = A.get(degA);
            int degB = B.firstKey();
            double coeffB = B.get(degB);

            if (degA < degB)
                break; // remainder degree < divisor degree

            double qCoeff = coeffA / coeffB;
            int qDeg = degA - degB;

            quotient.addTerm(qDeg, qCoeff);

            // subtract qCoeff * (divisor * x^qDeg) from A
            java.util.Map<Integer, Double> sub = new java.util.HashMap<>();
            for (var e : B.entrySet()) {
                int p = e.getKey() + qDeg;
                double c = e.getValue() * qCoeff;
                sub.put(p, c);
            }

            for (var e : sub.entrySet()) {
                int p = e.getKey();
                double c = e.getValue();
                double existing = A.getOrDefault(p, 0.0);
                double updated = existing - c;
                if (Math.abs(updated) < 1e-12)
                    A.remove(p);
                else
                    A.put(p, updated);
            }
        }

        // build remainder polynomial from leftover A
        if (!A.isEmpty()) {
            Polynomial rem = new Polynomial();
            for (var e : A.entrySet())
                rem.addTerm(e.getKey(), e.getValue());

            String remStr = rem.toString();
            String divStr = divisor.toString();
            quotient.remainderDisplay = "(" + remStr + ")/(" + divStr + ")";
        }

        return quotient.simplify();
    }

    // ---------- Simplify (combines like terms and removes near-zero) ----------
    public Polynomial simplify() {
        Polynomial r = new Polynomial();
        for (var e : this.terms.entrySet()) {
            if (Math.abs(e.getValue()) < 1e-12)
                continue;
            double v = Math.abs(e.getValue() - Math.round(e.getValue())) < 1e-12 ? Math.round(e.getValue())
                    : e.getValue();
            r.addTerm(e.getKey(), v);
        }
        r.remainderDisplay = this.remainderDisplay;
        return r;
    }

    // ---------- toString pretty print ----------
    private static String trimDouble(double v) {
        if (Double.isNaN(v) || Double.isInfinite(v))
            return String.valueOf(v);
        if (Math.abs(v - Math.round(v)) < 1e-12)
            return String.valueOf((long) Math.round(v));
        String s = String.valueOf(v);
        if (s.contains("E"))
            return s;
        if (s.indexOf('.') >= 0) {
            while (s.endsWith("0"))
                s = s.substring(0, s.length() - 1);
            if (s.endsWith("."))
                s = s.substring(0, s.length() - 1);
        }
        return s;
    }

    @Override
    public String toString() {
        if (terms.isEmpty()) {
            String base = "0";
            if (remainderDisplay != null)
                return base + " + " + remainderDisplay;
            return base;
        }

        java.util.List<Integer> powers = new java.util.ArrayList<>(terms.keySet());
        powers.sort((a, b) -> b - a);

        StringBuilder sb = new StringBuilder();
        boolean first = true;
        for (int p : powers) {
            double c = terms.get(p);
            if (Math.abs(c) < 1e-12)
                continue;
            String cs = trimDouble(c);

            if (first) {
                if (cs.startsWith("-")) {
                    sb.append(cs);
                } else {
                    sb.append(cs.equals("1") && p != 0 ? "" : cs);
                }
                first = false;
            } else {
                if (c < 0)
                    sb.append(" - ");
                else
                    sb.append(" + ");
                String acs = c < 0 ? trimDouble(-c) : trimDouble(c);
                sb.append(acs.equals("1") && p != 0 ? "" : acs);
            }

            if (p > 0) {
                sb.append("x");
                if (p != 1)
                    sb.append("^").append(p);
            }
        }

        String base = sb.toString();
        if (base.isEmpty())
            base = "0";
        if (remainderDisplay != null && !remainderDisplay.isEmpty()) {
            return base + " + " + remainderDisplay;
        }
        return base;
    }
}

// =======================================================
// EVALUATE FUNCTION SCREEN
// =======================================================

class EvaluateScreen extends JFrame {

    JPanel redPanel;
    JTextField funcField, xField;
    JEditorPane resultArea; // HTML renderer for safe <sup> usage

    // local superscript maps (so evaluate also supports pasted superscripts)
    private static final java.util.Map<Character, Character> SUP = new java.util.HashMap<>();
    private static final java.util.Map<Character, Character> UNSUP = new java.util.HashMap<>();
    static {
        SUP.put('0', '⁰');
        SUP.put('1', '¹');
        SUP.put('2', '²');
        SUP.put('3', '³');
        SUP.put('4', '⁴');
        SUP.put('5', '⁵');
        SUP.put('6', '⁶');
        SUP.put('7', '⁷');
        SUP.put('8', '⁸');
        SUP.put('9', '⁹');
        SUP.put('-', '⁻');
        SUP.put('+', '⁺');
        for (java.util.Map.Entry<Character, Character> e : SUP.entrySet()) {
            UNSUP.put(e.getValue(), e.getKey());
        }
    }

    public EvaluateScreen() {

        setTitle("Evaluate f(x)");
        setSize(600, 950);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setResizable(true);

        JPanel bg = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2 = (Graphics2D) g;
                g2.setPaint(new GradientPaint(0, 0, Palette.BG_TOP,
                        0, getHeight(), Palette.BG_BOTTOM));
                g2.fillRect(0, 0, getWidth(), getHeight());
            }
        };
        bg.setLayout(null);
        add(bg);

        redPanel = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2 = (Graphics2D) g;
                g2.setColor(Palette.EVAL_ACCENT);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 40, 40);
            }
        };

        redPanel.setLayout(null);
        redPanel.setSize(460, 750);
        redPanel.setOpaque(false);
        bg.add(redPanel);

        addComponentListener(new ComponentAdapter() {
            @Override
            public void componentResized(ComponentEvent e) {
                redPanel.setLocation((getWidth() - redPanel.getWidth()) / 2,
                        (getHeight() - redPanel.getHeight()) / 2);
            }
        });
        redPanel.setLocation((getWidth() - redPanel.getWidth()) / 2,
                (getHeight() - redPanel.getHeight()) / 2);

        JLabel title = new JLabel("EVALUATE f(x)", SwingConstants.CENTER);
        title.setBounds(40, 20, 380, 40);
        title.setForeground(Color.WHITE);
        title.setFont(new Font("Segoe UI", Font.BOLD, 26));
        redPanel.add(title);

        // funcField uses the shared createInputField (rounded + placeholder +
        // normalization)
        funcField = createInputField("Enter function e.g. x^2 + 3x - 1");
        funcField.setBounds(50, 90, 360, 50);
        funcField.setToolTipText("Enter function, e.g. x^2 + 3x - 1");
        redPanel.add(funcField);

        // xField created via the same createInputField so both fields look identical
        // visually
        xField = createInputField("Enter value of x");
        xField.setBounds(50, 160, 360, 50); // same height as funcField for visual match
        xField.setToolTipText("Enter value of x");
        redPanel.add(xField);

        JButton evalBtn = makeButton("Evaluate", 240);
        JButton backBtn = makeButton("Back", 310);

        redPanel.add(evalBtn);
        redPanel.add(backBtn);

        // === resultArea as JEditorPane (HTML) so we can render <sup> reliably ===
        resultArea = new JEditorPane();
        resultArea.setContentType("text/html");
        resultArea.setEditable(false);
        resultArea.putClientProperty(JEditorPane.HONOR_DISPLAY_PROPERTIES, Boolean.TRUE);

        // set same font as input field for consistent look
        Font inputFont = funcField.getFont();
        if (inputFont == null)
            inputFont = new Font("Segoe UI Semibold", Font.PLAIN, 20);
        resultArea.setFont(inputFont);

        // background & border similar to previous textarea style
        resultArea.setBackground(new Color(254, 250, 233));
        resultArea.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(Palette.DIFF_BUTTON.darker(), 3),
                BorderFactory.createEmptyBorder(10, 10, 10, 10)));

        JScrollPane scroll = new JScrollPane(resultArea);
        scroll.setBounds(50, 360, 360, 330);
        redPanel.add(scroll);

        evalBtn.addActionListener(e -> evaluateFx());
        backBtn.addActionListener(e -> {
            new MainMenuScreen().setVisible(true);
            dispose();
        });
    }

    private JButton makeButton(String text, int y) {
        JButton btn = new JButton(text) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g;
                Color bg = Palette.EVAL_BUTTON;
                if (getModel().isRollover())
                    bg = bg.brighter();
                if (getModel().isPressed())
                    bg = bg.darker();
                g2.setColor(bg);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 35, 35);
                super.paintComponent(g);
            }
        };
        btn.setBounds(50, y, 360, 50);
        btn.setFont(new Font("Segoe UI", Font.BOLD, 16));
        btn.setForeground(Palette.ACCENT_GENERIC);
        btn.setBorderPainted(false);
        btn.setContentAreaFilled(false);
        btn.setFocusPainted(false);
        return btn;
    }

    private JTextField createInputField(String placeholder) {
        JTextField f = new JTextField() {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(Color.WHITE);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 18, 18);
                super.paintComponent(g2);
                g2.dispose();
            }

            @Override
            protected void paintBorder(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(new Color(160, 160, 160));
                g2.setStroke(new BasicStroke(2));
                g2.drawRoundRect(1, 1, getWidth() - 3, getHeight() - 3, 18, 18);
                g2.dispose();
            }
        };
        f.setFont(new Font("Segoe UI Semibold", Font.PLAIN, 20));
        f.setForeground(Color.GRAY);
        f.setCaretColor(Color.BLACK);
        f.setBorder(BorderFactory.createEmptyBorder(10, 12, 10, 12));
        f.setText(placeholder);

        f.addFocusListener(new FocusAdapter() {
            @Override
            public void focusGained(FocusEvent e) {
                if (Color.GRAY.equals(f.getForeground())) {
                    f.setForeground(Color.BLACK);
                    f.setText("");
                }
            }

            @Override
            public void focusLost(FocusEvent e) {
                if (f.getText().trim().isEmpty()) {
                    f.setForeground(Color.GRAY);
                    f.setText(placeholder);
                    f.putClientProperty("rawNormalized", null);
                }
            }
        });

        f.addActionListener(e -> {
            String t = f.getText();
            if (t == null || t.isEmpty())
                return;
            String normalized = t;
            normalized = normalized.replaceAll("\\^\\s*\\(", "^(");
            normalized = normalized.replaceAll("\\^\\s*([+-]?\\d+)", "^($1)");

            Pattern p = Pattern.compile("\\^\\(([-+]?\\d+)\\)");
            Matcher m = p.matcher(normalized);
            StringBuffer sb = new StringBuffer();
            while (m.find()) {
                String expo = m.group(1);
                StringBuilder sup = new StringBuilder();
                for (char ch : expo.toCharArray()) {
                    sup.append(SUP.getOrDefault(ch, ch));
                }
                m.appendReplacement(sb, Matcher.quoteReplacement(sup.toString()));
            }
            m.appendTail(sb);

            f.setText(sb.toString());
            f.putClientProperty("rawNormalized", normalized);
        });

        return f;
    }

    // --- robust evaluate method (produces HTML output with <sup>) ---
    private void evaluateFx() {
        try {
            // ensure resultArea font matches input (uniform appearance)
            Font inputFont = funcField.getFont();
            if (inputFont == null)
                inputFont = new Font("Segoe UI Semibold", Font.PLAIN, 20);
            resultArea.setFont(inputFont);
            // FIX √x → sqrt(x) BEFORE evaluator
            String disp = funcField.getText().trim();
            disp = disp.replaceAll("√\\s*([A-Za-z0-9]+)", "sqrt($1)");
            disp = disp.replace("√(", "sqrt(");
            funcField.putClientProperty("rawNormalized", disp);

            // get expression (caret/raw form)
            String rawNormalized = (String) funcField.getClientProperty("rawNormalized");
            String fx = (rawNormalized != null && !rawNormalized.trim().isEmpty())
                    ? rawNormalized
                    : fromDisplayToRaw(funcField.getText());

            if (fx == null)
                fx = "";

            // sanitize and normalize
            fx = fx.replaceAll("[\\p{Cntrl}&&[^\r\n\t]]+", "");
            fx = fx.replace("–", "-").replace("—", "-").replace("−", "-");
            fx = fx.replaceAll("\\s+", "");
            fx = fx.replaceAll("\\^\\s*\\(", "^(");
            fx = fx.replaceAll("\\^\\s+", "^");

            fx = convertSuperscriptsToCaretSimple(fx);

            // validate x
            String xStr = xField.getText().trim();
            if (fx.isEmpty() || xStr.isEmpty() || Color.GRAY.equals(funcField.getForeground())) {
                setResultTextOnEDT("Please enter both function and x.");
                return;
            }

            double x;
            try {
                x = Double.parseDouble(xStr);
            } catch (NumberFormatException nfe) {
                setResultTextOnEDT("Invalid value for x. Enter a number (e.g., -3, 2, 3.5).");
                return;
            }

            // evaluate
            FunctionEvaluator fe = new FunctionEvaluator();
            double ans;
            try {
                ans = fe.evaluate(fx, x);
            } catch (Exception evalEx) {
                String emsg = evalEx.getMessage();
                if (emsg == null || emsg.trim().isEmpty())
                    emsg = evalEx.toString();
                setResultTextOnEDT("❌ ERROR evaluating function:\nExpression: " + fx + "\n\n" + emsg);
                return;
            }

            // prepare HTML using <sup>
            String funcHtml = renderSuperscriptsHTML(fx);
            String outHtml = "<html><body style='" + htmlCss() + "'>"
                    + "<div>f(x) = " + funcHtml + "</div>"
                    + "<br>"
                    + "<div>f(" + escapeHtml(trimDouble(x)) + ") = " + escapeHtml(trimDouble(ans)) + "</div>"
                    + "</body></html>";

            setResultTextOnEDT(outHtml, true);

        } catch (Exception ex) {
            String msg = ex.getMessage();
            if (msg == null || msg.trim().isEmpty())
                msg = ex.toString();
            setResultTextOnEDT("❌ ERROR evaluating function:\n" + msg);
        }
    }

    // set HTML/plain text on EDT (keeps UI consistent)
    private void setResultTextOnEDT(String htmlOrText, boolean isHtml) {
        SwingUtilities.invokeLater(() -> {
            if (!isHtml) {
                String h = "<html><body style='" + htmlCss() + "'>" + escapeHtml(htmlOrText).replaceAll("\n", "<br>")
                        + "</body></html>";
                resultArea.setText(h);
            } else {
                resultArea.setText(htmlOrText);
            }
            resultArea.setCaretPosition(0);
        });
    }

    // convenience wrapper (keeps older calls working)
    private void setResultTextOnEDT(String text) {
        setResultTextOnEDT(text, false);
    }

    // CSS string using the same font as funcField for consistent rendering
    private String htmlCss() {
        Font f = funcField.getFont();
        String fontFamily = (f != null) ? f.getFamily() : "Segoe UI Semibold";
        int fontSize = (f != null) ? f.getSize() : 20;
        // font-size in px for predictable sizing; color black
        return "font-family: '" + fontFamily + "', 'Segoe UI', sans-serif; font-size: " + fontSize
                + "px; color: #000000;";
    }

    // Convert caret-style exponents to HTML with <sup> (handles ^(digits) and
    // ^digits)
    private String renderSuperscriptsHTML(String raw) {
        if (raw == null || raw.isEmpty())
            return "";
        StringBuilder out = new StringBuilder();
        for (int i = 0; i < raw.length(); ++i) {
            char c = raw.charAt(i);
            if (c == '^') {
                // check ^(digits)
                if (i + 1 < raw.length() && raw.charAt(i + 1) == '(') {
                    int j = raw.indexOf(')', i + 2);
                    if (j != -1) {
                        String expo = raw.substring(i + 2, j);
                        out.append("<sup>").append(escapeHtml(expo)).append("</sup>");
                        i = j;
                        continue;
                    }
                }
                // check ^digit(s) immediately following
                int j = i + 1;
                StringBuilder expo = new StringBuilder();
                while (j < raw.length() && "+-0123456789".indexOf(raw.charAt(j)) != -1) {
                    expo.append(raw.charAt(j));
                    j++;
                }
                if (expo.length() > 0) {
                    out.append("<sup>").append(escapeHtml(expo.toString())).append("</sup>");
                    i = j - 1;
                    continue;
                }
                // fallback: keep caret
                out.append("^");
            } else {
                out.append(escapeHtml(String.valueOf(c)));
            }
        }
        return out.toString();
    }

    // basic HTML-escape (small helper)
    private String escapeHtml(String s) {
        if (s == null)
            return "";
        StringBuilder sb = new StringBuilder();
        for (char c : s.toCharArray()) {
            switch (c) {
                case '&':
                    sb.append("&amp;");
                    break;
                case '<':
                    sb.append("&lt;");
                    break;
                case '>':
                    sb.append("&gt;");
                    break;
                case '"':
                    sb.append("&quot;");
                    break;
                case '\'':
                    sb.append("&#39;");
                    break;
                default:
                    sb.append(c);
            }
        }
        return sb.toString();
    }

    // helper: convert any remaining unicode superscripts to caret digits (simple
    // one-pass)
    private String convertSuperscriptsToCaretSimple(String s) {
        if (s == null || s.isEmpty())
            return s;
        java.util.Map<Character, Character> MAP = new java.util.HashMap<>();
        MAP.put('⁰', '0');
        MAP.put('¹', '1');
        MAP.put('²', '2');
        MAP.put('³', '3');
        MAP.put('⁴', '4');
        MAP.put('⁵', '5');
        MAP.put('⁶', '6');
        MAP.put('⁷', '7');
        MAP.put('⁸', '8');
        MAP.put('⁹', '9');
        MAP.put('⁻', '-');
        MAP.put('⁺', '+');

        StringBuilder out = new StringBuilder();
        for (int i = 0; i < s.length(); ++i) {
            char c = s.charAt(i);
            if (MAP.containsKey(c)) {
                if (out.length() > 0 && out.charAt(out.length() - 1) == '^')
                    out.append(MAP.get(c));
                else {
                    out.append('^');
                    out.append(MAP.get(c));
                }
            } else
                out.append(c);
        }
        return out.toString();
    }

    // reuse or re-add trimDouble inside EvaluateScreen
    private String trimDouble(double v) {
        if (Double.isNaN(v) || Double.isInfinite(v))
            return String.valueOf(v);
        if (Math.abs(v - Math.round(v)) < 1e-12)
            return String.valueOf((long) Math.round(v));
        String s = String.valueOf(v);
        if (s.contains("E"))
            return s;
        if (s.indexOf('.') >= 0) {
            while (s.endsWith("0"))
                s = s.substring(0, s.length() - 1);
            if (s.endsWith("."))
                s = s.substring(0, s.length() - 1);
        }
        return s;
    }

    // Convert displayed string (may contain superscript Unicode) back to caret form
    // ^(digits)
    private String fromDisplayToRaw(String s) {
        if (s == null || s.isEmpty())
            return s;
        StringBuilder out = new StringBuilder();
        for (int i = 0; i < s.length();) {
            char c = s.charAt(i);
            if (UNSUP.containsKey(c)) {
                StringBuilder digits = new StringBuilder();
                while (i < s.length() && UNSUP.containsKey(s.charAt(i))) {
                    digits.append(UNSUP.get(s.charAt(i)));
                    i++;
                }
                out.append("^(").append(digits).append(")");
            } else {
                out.append(c);
                i++;
            }
        }
        return out.toString();
    }

    // Replace existing renderSuperscripts with this (safer)
private String renderSuperscripts(String raw) {
    if (raw == null || raw.isEmpty()) return raw;
    StringBuilder out = new StringBuilder();

    for (int i = 0; i < raw.length(); i++) {
        char c = raw.charAt(i);

        if (c == '^') {
            // case: ^( ... )  -> only convert to superscript if inside is purely digits or signed digits
            if (i + 1 < raw.length() && raw.charAt(i + 1) == '(') {
                int j = raw.indexOf(')', i + 2);
                if (j != -1) {
                    String expo = raw.substring(i + 2, j);
                    // only digits with optional leading +/-
                    if (expo.matches("[+-]?\\d+")) {
                        for (char ch : expo.toCharArray()) out.append(SUP.getOrDefault(ch, ch));
                    } else {
                        // keep caret + parentheses for complex exponent
                        out.append("^(").append(expo).append(")");
                    }
                    i = j;
                    continue;
                }
            }

            // case: simple ^3 or ^-2  -> convert
            if (i + 1 < raw.length()) {
                int j = i + 1;
                StringBuilder expo = new StringBuilder();
                while (j < raw.length() && "+-0123456789".indexOf(raw.charAt(j)) != -1) {
                    expo.append(raw.charAt(j));
                    j++;
                }
                if (expo.length() > 0) {
                    for (char ch : expo.toString().toCharArray())
                        out.append(SUP.getOrDefault(ch, ch));
                    i = j - 1;
                    continue;
                }
            }
        }

        out.append(c);
    }

    return out.toString();
}

}

// ======================================================
// FUNCTION EVALUATOR
// ======================================================
class FunctionEvaluator {

    static class Node {
        String val;
        Node left, right;

        Node(String v) {
            val = v;
        }
    }

    // map for converting unicode superscripts back to digits (if present)
    private static final java.util.Map<Character, Character> SUP_TO_DIGIT = new java.util.HashMap<>();
    static {
        SUP_TO_DIGIT.put('⁰', '0');
        SUP_TO_DIGIT.put('¹', '1');
        SUP_TO_DIGIT.put('²', '2');
        SUP_TO_DIGIT.put('³', '3');
        SUP_TO_DIGIT.put('⁴', '4');
        SUP_TO_DIGIT.put('⁵', '5');
        SUP_TO_DIGIT.put('⁶', '6');
        SUP_TO_DIGIT.put('⁷', '7');
        SUP_TO_DIGIT.put('⁸', '8');
        SUP_TO_DIGIT.put('⁹', '9');
        SUP_TO_DIGIT.put('⁻', '-');
        SUP_TO_DIGIT.put('⁺', '+');
    }

    // ---------- Helpers ----------
    private String convertSuperscriptsToCaret(String s) {
        if (s == null || s.isEmpty())
            return s;
        StringBuilder out = new StringBuilder();
        for (int i = 0; i < s.length(); ++i) {
            char c = s.charAt(i);
            if (SUP_TO_DIGIT.containsKey(c)) {
                if (out.length() > 0 && out.charAt(out.length() - 1) == '^')
                    out.append(SUP_TO_DIGIT.get(c));
                else {
                    out.append('^');
                    out.append(SUP_TO_DIGIT.get(c));
                }
            } else {
                out.append(c);
            }
        }
        return out.toString();
    }

    private String sanitize(String s) {
        if (s == null)
            return "";
        String t = s;
        // normalize minus/dashes
        t = t.replace("–", "-").replace("—", "-").replace("−", "-");
        // convert square root symbol to sqrt token
        t = t.replace("√(", "sqrt(");
        t = t.replace("√", "sqrt");
        // convert superscripts if pasted
        t = convertSuperscriptsToCaret(t);
        // collapse whitespace
        t = t.replaceAll("\\s+", "");
        // Keep pi lower-case; avoid aggressive replacements
        t = t.replaceAll("(?i)PI", "pi");
        return t;
    }

    private String attachFuncParens(String s) {
        if (s == null || s.isEmpty())
            return s;
        // attach parentheses for common funcs when user types sinx, sqrtx, etc.
        // Use case-insensitive recognition for letters, then fold to lower-case tokens
        // We'll replace patterns like sinx -> sin(x)
        String t = s;
        // do a few specific replacements (function names in lower-case)
        t = t.replaceAll("(?i)(sin|cos|tan|asin|acos|atan|sinh|cosh|tanh|ln|exp|sqrt|log|sec|csc|cot)(?=x)", "$1(x)");
        return t;
    }

    // ---------- Predicates ----------
    boolean isNum(String s) {
        return s != null && s.matches("-?\\d+(\\.\\d+)?");
    }

    boolean isVar(String s) {
        return "x".equals(s) || "X".equals(s);
    }

    boolean isFunc(String s) {
        if (s == null)
            return false;
        s = s.toLowerCase();
        return s.equals("sin") || s.equals("cos") || s.equals("tan") ||
                s.equals("ln") || s.equals("exp") || s.equals("sqrt") ||
                s.equals("log") || s.equals("asin") || s.equals("acos") || s.equals("atan") ||
                s.equals("sec") || s.equals("csc") || s.equals("cot") ||
                s.equals("sinh") || s.equals("cosh") || s.equals("tanh") ||
                s.equals("asinh") || s.equals("acosh") || s.equals("atanh");
    }

    // ---------- TOKENIZER ----------
    ArrayList<String> tokenize(String s) {
        ArrayList<String> out = new ArrayList<>();
        if (s == null || s.isEmpty())
            return out;
        StringBuilder cur = new StringBuilder();
        for (int i = 0; i < s.length(); ++i) {
            char c = s.charAt(i);
            if (Character.isLetter(c)) {
                cur.append(c);
                if (i < s.length() - 1 && Character.isLetter(s.charAt(i + 1)))
                    continue;
                out.add(cur.toString().toLowerCase());
                cur.setLength(0);
            } else if (Character.isDigit(c) || c == '.') {
                cur.append(c);
                if (i < s.length() - 1 && (Character.isDigit(s.charAt(i + 1)) || s.charAt(i + 1) == '.'))
                    continue;
                out.add(cur.toString());
                cur.setLength(0);
            } else {
                if (cur.length() > 0) {
                    out.add(cur.toString());
                    cur.setLength(0);
                }
                if (!Character.isWhitespace(c))
                    out.add(String.valueOf(c));
            }
        }
        return out;
    }

    // ---------- FIX TOKENS: handle unary minus, implicit multiplication, and sqrt
    // x -> sqrt(x) ----------
    ArrayList<String> fixTokens(ArrayList<String> t) {
        ArrayList<String> out = new ArrayList<>();

        for (int i = 0; i < t.size(); ++i) {
            String a = t.get(i);

            // handle unary minus
            if ("-".equals(a)) {
                boolean unary = (i == 0) || "(".equals(t.get(i - 1)) || "+-*/^".contains(t.get(i - 1));
                if (unary) {
                    out.add("0");
                    out.add("-");
                    continue;
                }
            }

            out.add(a);

            if (i < t.size() - 1) {
                String b = t.get(i + 1);

                // If left is FUNCTION and right is VARIABLE/NUMBER -> interpret as function
                // call: f x -> f(x)
                if (isFunc(a) && (isVar(b) || isNum(b))) {
                    // remove the last added function token
                    out.remove(out.size() - 1);
                    out.add(a); // function name
                    out.add("(");
                    out.add(b);
                    out.add(")");
                    i++; // skip b
                    continue;
                }

                // do not insert * between function and '('
                if (isFunc(a) && "(".equals(b)) {
                    // nothing to add
                    continue;
                }

                boolean left = isNum(a) || isVar(a) || ")".equals(a) || isFunc(a);
                boolean right = isNum(b) || isVar(b) || "(".equals(b) || isFunc(b);
                if (left && right)
                    out.add("*");
            }
        }

        return out;
    }

    // precedence (used by shunting-yard)
    private int prec(String op) {
        if (op == null)
            return 0;
        if (op.equals("+") || op.equals("-"))
            return 1;
        if (op.equals("*") || op.equals("/"))
            return 2;
        if (op.equals("^"))
            return 3;
        if (isFunc(op))
            return 4;
        return 0;
    }

    // ---------- INFIX -> POSTFIX (right-assoc ^) ----------
    ArrayList<String> infixToPostfix(ArrayList<String> t) {
        Stack<String> st = new Stack<>();
        ArrayList<String> out = new ArrayList<>();
        for (String s : t) {
            if (isNum(s) || isVar(s) || "pi".equals(s) || "e".equals(s))
                out.add(s);
            else if (isFunc(s))
                st.push(s);
            else if ("(".equals(s))
                st.push(s);
            else if (")".equals(s)) {
                while (!st.isEmpty() && !st.peek().equals("("))
                    out.add(st.pop());
                if (st.isEmpty())
                    throw new IllegalArgumentException("Mismatched parentheses.");
                st.pop();
                if (!st.isEmpty() && isFunc(st.peek()))
                    out.add(st.pop());
            } else { // operator
                while (!st.isEmpty() && !st.peek().equals("(")) {
                    String top = st.peek();
                    int pTop = prec(top);
                    int pS = prec(s);
                    if (pTop > pS)
                        out.add(st.pop());
                    else if (pTop == pS) {
                        if (s.equals("^"))
                            break; // right-assoc ^ -> don't pop same precedence
                        else
                            out.add(st.pop()); // left-assoc operators pop on equal precedence
                    } else
                        break;
                }
                st.push(s);
            }
        }
        while (!st.isEmpty()) {
            String top = st.pop();
            if ("(".equals(top) || ")".equals(top))
                throw new IllegalArgumentException("Mismatched parentheses.");
            out.add(top);
        }
        return out;
    }

    // ---------- BUILD TREE ----------
    Node buildTree(ArrayList<String> p) {
        Stack<Node> st = new Stack<>();
        for (String s : p) {
            if (isNum(s) || isVar(s) || "pi".equals(s) || "e".equals(s)) {
                st.push(new Node(s));
            } else if (isFunc(s)) {
                if (st.isEmpty())
                    throw new IllegalArgumentException("Missing function argument for " + s);
                Node a = st.pop();
                Node f = new Node(s);
                f.left = a;
                st.push(f);
            } else { // operator
                if (st.size() < 2)
                    throw new IllegalArgumentException("Invalid operator usage for " + s);
                Node b = st.pop();
                Node a = st.pop();
                Node op = new Node(s);
                op.left = a;
                op.right = b;
                st.push(op);
            }
        }
        if (st.isEmpty())
            throw new IllegalArgumentException("Empty expression.");
        return st.pop();
    }

    // ---------- EVALUATION ----------
    double eval(Node n, double x) {
        if (isNum(n.val))
            return Double.parseDouble(n.val);
        if (isVar(n.val))
            return x;
        if ("pi".equals(n.val))
            return Math.PI;
        if ("e".equals(n.val))
            return Math.E;

        double L = (n.left != null) ? eval(n.left, x) : 0;
        double R = (n.right != null) ? eval(n.right, x) : 0;

        switch (n.val) {
            case "+":
                return L + R;
            case "-":
                return L - R;
            case "*":
                return L * R;
            case "/":
                return L / R;
            case "^":
                return Math.pow(L, R);
            case "sin":
                return Math.sin(L);
            case "cos":
                return Math.cos(L);
            case "tan":
                return Math.tan(L);
            case "ln":
                return Math.log(L);
            case "log":
                return Math.log10(L);
            case "exp":
                return Math.exp(L);
            case "sqrt":
                return Math.sqrt(L);
            case "asin":
                return Math.asin(L);
            case "acos":
                return Math.acos(L);
            case "atan":
                return Math.atan(L);
            case "sinh":
                return Math.sinh(L);
            case "cosh":
                return Math.cosh(L);
            case "tanh":
                return Math.tanh(L);
            case "sec":
                return 1.0 / Math.cos(L);
            case "csc":
                return 1.0 / Math.sin(L);
            case "cot":
                return 1.0 / Math.tan(L);
            case "asinh":
                return Math.log(L + Math.sqrt(L * L + 1));
            case "acosh":
                return Math.log(L + Math.sqrt(L * L - 1));
            case "atanh":
                return 0.5 * Math.log((1 + L) / (1 - L));
            default:
                throw new IllegalArgumentException("Unknown node: " + n.val);
        }
    }

    // ---------- PUBLIC API ----------
    double evaluate(String expr, double x) {
        if (expr == null)
            expr = "";
        // sanitize & normalize: convert √ to sqrt, convert superscripts etc.
        String t = sanitize(expr);
        // attach parentheses for forms like sinx, sqrtx -> sin(x), sqrt(x)
        t = attachFuncParens(t);

        // quick special-case top-level sqrt(...) (avoid parser edgecases)
        try {
            java.util.regex.Pattern sqrtPat = java.util.regex.Pattern.compile("^sqrt\\((.*)\\)$");
            java.util.regex.Matcher sqrtM = sqrtPat.matcher(t);
            if (sqrtM.matches()) {
                String inner = sqrtM.group(1);
                double innerVal = evaluate(inner, x);
                return Math.sqrt(innerVal);
            }
        } catch (StackOverflowError so) {
            // ignore and continue to full parse
        }

        ArrayList<String> tokens = tokenize(t);
        tokens = fixTokens(tokens);
        ArrayList<String> post = infixToPostfix(tokens);
        Node root = buildTree(post);
        return eval(root, x);
    }
}

// ======================================================
// MAIN MENU SCREEN
// ======================================================
class MainMenuScreen extends JFrame {

    JPanel redPanel;

    public MainMenuScreen() {

        setTitle("Calculus Toolkit - Menu");
        setSize(550, 900);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setResizable(true);

        JPanel mainPanel = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);

                Graphics2D g2 = (Graphics2D) g;
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                GradientPaint gp = new GradientPaint(
                        0, 0, Palette.BG_TOP,
                        0, getHeight(), Palette.BG_BOTTOM);
                g2.setPaint(gp);
                g2.fillRect(0, 0, getWidth(), getHeight());
            }
        };

        mainPanel.setLayout(null);
        add(mainPanel);

        redPanel = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);

                Graphics2D g2 = (Graphics2D) g;
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                g2.setColor(Palette.MENU_ACCENT);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 40, 40);
            }
        };

        redPanel.setLayout(null);
        redPanel.setSize(400, 550);
        redPanel.setOpaque(false);
        mainPanel.add(redPanel);

        centerPanel();
        addComponentListener(new ComponentAdapter() {
            @Override
            public void componentResized(ComponentEvent e) {
                centerPanel();
            }
        });

        JLabel title = new JLabel("CALCULUS MENU", SwingConstants.CENTER);
        title.setFont(new Font("Segoe UI", Font.BOLD, 26));
        title.setForeground(Color.WHITE);
        title.setBounds(25, 25, 350, 40);
        redPanel.add(title);

        // ---------- BUTTONS ----------
        JButton diffBtn = createMenuButton("Differentiate", 150);
        diffBtn.addActionListener(e -> {
            new DifferentiationScreen().setVisible(true);
            dispose();
        });
        redPanel.add(diffBtn);

        JButton evalBtn = createMenuButton("Evaluate f(x)", 230);
        evalBtn.addActionListener(e -> {
            new EvaluateScreen().setVisible(true);
            dispose();
        });
        redPanel.add(evalBtn);

        JButton polyBtn = createMenuButton("Polynomial Tools", 310);
        polyBtn.addActionListener(e -> {
            new PolynomialToolsScreen().setVisible(true);
            dispose();
        });
        redPanel.add(polyBtn);

        JButton backBtn = createMenuButton("Back", 390);
        backBtn.addActionListener(e -> {
            new CalculusToolkit().setVisible(true);
            dispose();
        });
        redPanel.add(backBtn);
    }

    private void centerPanel() {
        int frameW = getWidth();
        int frameH = getHeight();
        int pW = redPanel.getWidth();
        int pH = redPanel.getHeight();
        int x = (frameW - pW) / 2;
        int y = (frameH - pH) / 2;
        redPanel.setLocation(x, y);
    }

    private JButton createMenuButton(String text, int y) {
        JButton btn = new JButton(text) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g;
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                Color bg = Palette.MENU_BUTTON;
                if (getModel().isRollover())
                    bg = bg.brighter();
                if (getModel().isPressed())
                    bg = bg.darker();

                g2.setColor(bg);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 35, 35);
                super.paintComponent(g);
            }
        };

        btn.setFont(new Font("Segoe UI", Font.BOLD, 18));
        btn.setForeground(Palette.ACCENT_GENERIC);
        btn.setFocusPainted(false);
        btn.setBorderPainted(false);
        btn.setContentAreaFilled(false);
        btn.setOpaque(false);
        btn.setBounds(50, y, 300, 55);
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));

        return btn;
    }
}
