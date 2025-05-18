import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.*;
import java.util.List;

public class GraphShortestPathGUI extends JFrame {
    private int[][] adjacencyMatrix;
    private int size;
    private JTextField[][] matrixFields;
    private JPanel matrixPanel, graphPanel, controlPanel;
    private JComboBox<String> startCombo, endCombo;
    private JButton generateButton, pathButton;
    private List<Integer> shortestPath;
    private int shortestCost;

    public GraphShortestPathGUI() {
        setTitle("Graph Shortest Path Visualizer");
        setSize(900, 650);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLayout(new BorderLayout());
        getContentPane().setBackground(Color.WHITE);

        matrixPanel = new JPanel();
        graphPanel = new GraphPanel();
        controlPanel = new JPanel();

        askMatrixSize();
    }

    private void askMatrixSize() {
        String input = JOptionPane.showInputDialog(this, "Enter number of users (nodes):", "Matrix Size",
                JOptionPane.QUESTION_MESSAGE);
        if (input == null)
            return;
        try {
            size = Integer.parseInt(input);
            if (size <= 1)
                throw new NumberFormatException();
            buildMatrixInput();
        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(this, "Please enter a valid number > 1.");
            askMatrixSize();
        }
    }

    private void buildMatrixInput() {
        matrixPanel.removeAll();
        matrixPanel.setLayout(new GridLayout(size + 1, size + 1, 2, 2));
        matrixFields = new JTextField[size][size];

        matrixPanel.add(new JLabel(""));
        for (int i = 0; i < size; i++) {
            JLabel label = new JLabel("U" + i, SwingConstants.CENTER);
            label.setFont(new Font("Arial", Font.BOLD, 12));
            matrixPanel.add(label);
        }

        for (int i = 0; i < size; i++) {
            JLabel label = new JLabel("U" + i);
            label.setFont(new Font("Arial", Font.BOLD, 12));
            matrixPanel.add(label);
            for (int j = 0; j < size; j++) {
                matrixFields[i][j] = new JTextField("0");
                matrixFields[i][j].setHorizontalAlignment(JTextField.CENTER);
                matrixFields[i][j].setPreferredSize(new Dimension(40, 40));
                matrixFields[i][j].setFont(new Font("Arial", Font.PLAIN, 12));
                matrixPanel.add(matrixFields[i][j]);
            }
        }

        generateButton = new JButton("Generate Graph");
        styleButton(generateButton);
        generateButton.addActionListener(e -> generateGraph());

        JPanel wrapper = new JPanel(new BorderLayout());
        wrapper.add(matrixPanel, BorderLayout.CENTER);
        wrapper.add(generateButton, BorderLayout.SOUTH);
        wrapper.setBorder(BorderFactory.createTitledBorder("Adjacency Matrix"));

        add(wrapper, BorderLayout.WEST);
        add(graphPanel, BorderLayout.CENTER);
        revalidate();
    }

    private void styleButton(JButton button) {
        button.setBackground(new Color(70, 130, 180));
        button.setForeground(Color.WHITE);
        button.setFont(new Font("Arial", Font.BOLD, 13));
        button.setFocusPainted(false);
        button.setBorder(BorderFactory.createEmptyBorder(5, 15, 5, 15));
    }

    private void generateGraph() {
        adjacencyMatrix = new int[size][size];
        try {
            for (int i = 0; i < size; i++)
                for (int j = 0; j < size; j++)
                    adjacencyMatrix[i][j] = Integer.parseInt(matrixFields[i][j].getText());

            buildControlPanel();
            repaint();
        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(this, "Invalid number in matrix.");
        }
    }

    private void buildControlPanel() {
        controlPanel.removeAll();
        controlPanel.setLayout(new FlowLayout(FlowLayout.CENTER, 15, 10));
        controlPanel.setBackground(Color.WHITE);

        String[] users = new String[size];
        for (int i = 0; i < size; i++)
            users[i] = "U" + i;

        startCombo = new JComboBox<>(users);
        endCombo = new JComboBox<>(users);
        pathButton = new JButton("Find Shortest Path");
        styleButton(pathButton);

        pathButton.addActionListener(e -> {
            int start = startCombo.getSelectedIndex();
            int end = endCombo.getSelectedIndex();
            calculateShortestPath(start, end);
            repaint();
        });

        controlPanel.add(new JLabel("Start:"));
        controlPanel.add(startCombo);
        controlPanel.add(new JLabel("End:"));
        controlPanel.add(endCombo);
        controlPanel.add(pathButton);

        add(controlPanel, BorderLayout.SOUTH);
        revalidate();
    }

    private void calculateShortestPath(int start, int end) {
        int[] dist = new int[size];
        boolean[] visited = new boolean[size];
        int[] prev = new int[size];

        Arrays.fill(dist, Integer.MAX_VALUE);
        Arrays.fill(prev, -1);
        dist[start] = 0;

        for (int i = 0; i < size; i++) {
            int u = getMinDistNode(dist, visited);
            if (u == -1)
                break;
            visited[u] = true;

            for (int v = 0; v < size; v++) {
                if (adjacencyMatrix[u][v] > 0 && !visited[v]) {
                    int alt = dist[u] + adjacencyMatrix[u][v];
                    if (alt < dist[v]) {
                        dist[v] = alt;
                        prev[v] = u;
                    }
                }
            }
        }

        shortestPath = new ArrayList<>();
        for (int at = end; at != -1; at = prev[at])
            shortestPath.add(0, at);

        shortestCost = dist[end];
    }

    private int getMinDistNode(int[] dist, boolean[] visited) {
        int min = Integer.MAX_VALUE, idx = -1;
        for (int i = 0; i < size; i++)
            if (!visited[i] && dist[i] < min) {
                min = dist[i];
                idx = i;
            }
        return idx;
    }

    class GraphPanel extends JPanel {
        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            setBackground(Color.WHITE);
            if (adjacencyMatrix == null)
                return;

            Graphics2D g2 = (Graphics2D) g;
            int radius = 220;
            int centerX = getWidth() / 2;
            int centerY = getHeight() / 2;
            int nodeRadius = 25;

            Point[] nodePoints = new Point[size];

            // Draw nodes
            for (int i = 0; i < size; i++) {
                double angle = 2 * Math.PI * i / size;
                int x = centerX + (int) (radius * Math.cos(angle));
                int y = centerY + (int) (radius * Math.sin(angle));
                nodePoints[i] = new Point(x, y);

                g2.setColor(new Color(180, 205, 230));
                g2.fillOval(x - nodeRadius, y - nodeRadius, 2 * nodeRadius, 2 * nodeRadius);
                g2.setColor(Color.BLACK);
                g2.setFont(new Font("Arial", Font.BOLD, 12));
                g2.drawString("U" + i, x - 8, y + 5);
            }

            // Draw edges
            g2.setStroke(new BasicStroke(1));
            for (int i = 0; i < size; i++) {
                for (int j = i + 1; j < size; j++) {
                    if (adjacencyMatrix[i][j] > 0) {
                        g2.setColor(Color.GRAY);
                        g2.drawLine(nodePoints[i].x, nodePoints[i].y, nodePoints[j].x, nodePoints[j].y);

                        // Draw weight
                        int mx = (nodePoints[i].x + nodePoints[j].x) / 2;
                        int my = (nodePoints[i].y + nodePoints[j].y) / 2;
                        g2.setFont(new Font("Arial", Font.PLAIN, 11));
                        g2.drawString(String.valueOf(adjacencyMatrix[i][j]), mx, my);
                    }
                }
            }

            // Highlight shortest path
            if (shortestPath != null && shortestPath.size() > 1) {
                g2.setStroke(new BasicStroke(3));
                g2.setColor(Color.RED);
                for (int i = 0; i < shortestPath.size() - 1; i++) {
                    int from = shortestPath.get(i);
                    int to = shortestPath.get(i + 1);
                    g2.drawLine(nodePoints[from].x, nodePoints[from].y, nodePoints[to].x, nodePoints[to].y);
                }

                // Show cost
                g2.setColor(new Color(20, 80, 180));
                g2.setFont(new Font("Arial", Font.BOLD, 18));
                g2.drawString("Total Cost: " + shortestCost, 20, 30);
            }
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            GraphShortestPathGUI gui = new GraphShortestPathGUI();
            gui.setVisible(true);
        });
    }
}
