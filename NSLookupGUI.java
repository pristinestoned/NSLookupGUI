import javax.swing.*;
import java.awt.*;
import java.awt.datatransfer.StringSelection;
import java.net.*;
import java.util.List;

public class NSLookupGUI extends JFrame {

    private JTextField hostField;
    private JTextArea outputArea;
    private JComboBox<String> lookupType;

    public NSLookupGUI() {
        setTitle("Java NSLookup Utility");
        setSize(750, 550);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        initializeGUI();
    }

    private void initializeGUI() {

        JPanel mainPanel = new JPanel(new BorderLayout(10, 10));
        mainPanel.setBorder(
            BorderFactory.createEmptyBorder(12, 12, 12, 12)
        );

        // Input panel
        JPanel inputPanel = new JPanel(new BorderLayout(8, 8));

        hostField = new JTextField();
        hostField.setFont(new Font("SansSerif", Font.PLAIN, 14));

        lookupType = new JComboBox<>(new String[]{
            "All Records",
            "IPv4 (A)",
            "IPv6 (AAAA)",
            "Reverse Lookup"
        });

        JButton lookupButton = new JButton("Lookup");
        lookupButton.addActionListener(e -> performLookup());

        hostField.addActionListener(e -> performLookup());

        JPanel controls = new JPanel(new FlowLayout(FlowLayout.LEFT));
        controls.add(new JLabel("Lookup Type:"));
        controls.add(lookupType);
        controls.add(lookupButton);

        inputPanel.add(new JLabel("Hostname / IP Address:"), BorderLayout.NORTH);
        inputPanel.add(hostField, BorderLayout.CENTER);
        inputPanel.add(controls, BorderLayout.SOUTH);

        // Output panel
        outputArea = new JTextArea();
        outputArea.setEditable(false);
        outputArea.setFont(new Font("Monospaced", Font.PLAIN, 13));
        outputArea.setLineWrap(true);
        outputArea.setWrapStyleWord(true);

        JScrollPane scrollPane = new JScrollPane(outputArea);

        // Bottom buttons
        JButton clearButton = new JButton("Clear");
        clearButton.addActionListener(e -> outputArea.setText(""));

        JButton copyButton = new JButton("Copy Results");
        copyButton.addActionListener(e -> {
            StringSelection selection =
                new StringSelection(outputArea.getText());

            Toolkit.getDefaultToolkit()
                .getSystemClipboard()
                .setContents(selection, null);
        });

        JPanel bottomPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        bottomPanel.add(copyButton);
        bottomPanel.add(clearButton);

        mainPanel.add(inputPanel, BorderLayout.NORTH);
        mainPanel.add(scrollPane, BorderLayout.CENTER);
        mainPanel.add(bottomPanel, BorderLayout.SOUTH);

        add(mainPanel);

        outputArea.setText(
            "Java NSLookup Utility\n" +
            "----------------------------------------\n" +
            "Enter a hostname or IP address and click Lookup.\n"
        );
    }

    private void performLookup() {

        String host = hostField.getText().trim();

        if (host.isEmpty()) {
            JOptionPane.showMessageDialog(
                this,
                "Please enter a hostname or IP address.",
                "Input Required",
                JOptionPane.WARNING_MESSAGE
            );
            return;
        }

        String type = (String) lookupType.getSelectedItem();

        outputArea.setText(
            "Performing DNS lookup...\n\nHostname: " + host + "\n"
        );

        SwingWorker<String, Void> worker =
            new SwingWorker<String, Void>() {

            @Override
            protected String doInBackground() throws Exception {
                StringBuilder result = new StringBuilder();

                result.append("NSLOOKUP RESULTS\n");
                result.append("========================================\n");
                result.append("Host: ").append(host).append("\n");
                result.append("Lookup: ").append(type).append("\n\n");

                switch (type) {

                    case "IPv4 (A)":
                        lookupAddresses(host, Inet4Address.class, result);
                        break;

                    case "IPv6 (AAAA)":
                        lookupAddresses(host, Inet6Address.class, result);
                        break;

                    case "Reverse Lookup":
                        reverseLookup(host, result);
                        break;

                    default:
                        lookupAddresses(host, InetAddress.class, result);
                        break;
                }

                return result.toString();
            }

            @Override
            protected void done() {
                try {
                    outputArea.setText(get());
                } catch (Exception ex) {
                    outputArea.setText(
                        "Lookup failed:\n" + ex.getMessage()
                    );
                }
            }
        };

        worker.execute();
    }

    private void lookupAddresses(
        String host,
        Class<?> addressType,
        StringBuilder result
    ) throws UnknownHostException {

        InetAddress[] addresses = InetAddress.getAllByName(host);

        int count = 0;

        for (InetAddress address : addresses) {

            if (addressType != InetAddress.class &&
                !addressType.isInstance(address)) {
                continue;
            }

            count++;

            result.append("Address ").append(count).append("\n");
            result.append("----------------------------------------\n");

            result.append("IP Address: ")
                  .append(address.getHostAddress()).append("\n");

            result.append("Canonical Name: ")
                  .append(address.getCanonicalHostName()).append("\n");

            result.append("Address Type: ")
                  .append(address instanceof Inet4Address
                      ? "IPv4" : "IPv6")
                  .append("\n\n");
        }

        if (count == 0) {
            result.append("No matching addresses found.\n");
        }
    }

    private void reverseLookup(
        String host,
        StringBuilder result
    ) throws UnknownHostException {

        InetAddress address = InetAddress.getByName(host);

        result.append("IP Address: ")
              .append(address.getHostAddress()).append("\n");

        result.append("Reverse Hostname: ")
              .append(address.getCanonicalHostName()).append("\n");

        result.append("\nNote: A reverse hostname is not proof that\n");
        result.append("a PTR record exists; verify with a DNS PTR query.\n");
    }

    public static void main(String[] args) {

        SwingUtilities.invokeLater(() -> {
            try {
                UIManager.setLookAndFeel(
                    UIManager.getSystemLookAndFeelClassName()
                );
            } catch (Exception ignored) {
            }

            NSLookupGUI gui = new NSLookupGUI();
            gui.setVisible(true);
        });
    }
}
