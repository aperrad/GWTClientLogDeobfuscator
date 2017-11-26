package GWTClientLogDeobfuscator;

import java.awt.BorderLayout;
import java.awt.Container;
import java.awt.Font;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.swing.BorderFactory;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.WindowConstants;

/**
 *
 * @author aperrad
 */
public class GWTClientLogDeobfuscatorGUI extends JFrame {

	private static final Logger LOGGER = Logger.getLogger(GWTClientLogDeobfuscatorGUI.class.getName());

	private static final long serialVersionUID = -8122737123593799403L;

	private JButton browseLogFile;

	private JLabel browseLogFileLabel;

	private JButton browseOutputPathDirectory;

	private JLabel browseOutputPathDirectoryLabel;

	private JButton browseSymbolMap;

	private JLabel browseSymbolMapLabel;

	private JButton browseWarFile;

	private JLabel browseWarFileLabel;

	private JButton close;

	private JButton deobfuscate;

	private JTextField localeField;

	private JLabel localeLabel;

	private JLabel titleLabel;

	private JComboBox<String> webBrowserChoicelist;

	private JLabel webBrowserLabel;

	private final JFileChooser fileBrowser = new JFileChooser();
	private String outputDirectory = "";

	private String symbolMapPath = "";

	private String warPath = "";

	private String logFilePath = "";

	private String fileName;

	public GWTClientLogDeobfuscatorGUI() {
		initComponents();
	}

	private void initComponents() {

		setTitle("GWT Client Log Deobfuscator");
		setLocationRelativeTo(null); // Center app in screen
		setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
		instantiateComponents();

		// buildTitle();

		buildBrowseLogFile();
		buildBrowseSymbolMap();
		buildBrowseWarFile();
		buildBrowseOutputPath();
		buildLocaleField();
		buildWebbrowserChoicelist();

		buildAppActions();

		buildAppLayout();
	}

	private void buildTitle() {
		titleLabel.setFont(new Font("Lucida Grande", 0, 18));
		titleLabel.setText("GWT Client Log Debofuscator");
	}

	private void buildBrowseLogFile() {
		browseLogFileLabel.setText("Log file to deobfuscate");

		browseLogFile.setText("Browse");
		browseLogFile.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent evt) {
				File file = getFile(JFileChooser.FILES_ONLY);
				logFilePath = file.getAbsolutePath();
				fileName = file.getName();
				changeButtonState(browseLogFile, false);
			}
		});
	}

	private File getFile(int selectionMode) {
		fileBrowser.setFileSelectionMode(selectionMode);
		int returnVal = fileBrowser.showOpenDialog(GWTClientLogDeobfuscatorGUI.this);
		if (returnVal == JFileChooser.APPROVE_OPTION) {
			File file = fileBrowser.getSelectedFile();
			return file;
		} else {
			LOGGER.info("Open command cancelled by user.");
			return null;
		}
	}

	private void buildBrowseSymbolMap() {
		browseSymbolMapLabel.setText("Symbol map file");

		browseSymbolMap.setText("Browse");
		browseSymbolMap.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				File file = getFile(JFileChooser.FILES_ONLY);
				symbolMapPath = file.getAbsolutePath();
				changeButtonState(browseSymbolMap, false);
			}
		});
	}

	private void buildBrowseWarFile() {
		browseWarFileLabel.setText("War file");

		browseWarFile.setText("Browse");
		browseWarFile.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent evt) {
				File file = getFile(JFileChooser.FILES_ONLY);
				warPath = file.getAbsolutePath();
				changeButtonState(browseWarFile, false);
			}
		});
	}

	private void changeButtonState(JButton button, boolean enable) {
		button.setEnabled(enable);
	}

	private void buildBrowseOutputPath() {
		browseOutputPathDirectoryLabel.setText("Output path directory");

		browseOutputPathDirectory.setText("Browse");
		browseOutputPathDirectory.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent evt) {
				File file = getFile(JFileChooser.DIRECTORIES_ONLY);
				outputDirectory = file.getAbsolutePath();
				changeButtonState(browseOutputPathDirectory, false);
			}
		});
	}

	private void buildLocaleField() {
		localeLabel.setText("Locale");
	}

	private void buildWebbrowserChoicelist() {
		webBrowserLabel.setText("Web Browser");
		webBrowserChoicelist.setModel(new DefaultComboBoxModel<>(new String[] { "", "Chrome", "Firefox", "Safari" }));
	}

	private String getLocalValue() {
		return localeField.getText();
	}

	private String getWebBrowserValue() {
		return webBrowserChoicelist.getItemAt(webBrowserChoicelist.getSelectedIndex());
	}

	private String getOutputFilePath() {
		return outputDirectory + "/" + fileName + ".deobfuscated";
	}

	private void buildAppActions() {
		close.setText("Close");
		close.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent evt) {
				dispose();
				System.exit(0);
			}
		});

		deobfuscate.setText("Deobfuscate");
		deobfuscate.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent evt) {
				try {
					GWTClientLogDeobfuscator.deobfuscate(logFilePath, warPath, symbolMapPath, getOutputFilePath(),
							getWebBrowserValue(), getLocalValue(), "", "");
					changeButtonState(browseLogFile, true);
					changeButtonState(browseOutputPathDirectory, true);
					changeButtonState(browseSymbolMap, true);
					changeButtonState(browseWarFile, true);
					if (!GWTClientLogDeobfuscator.getMessage().isEmpty()) {
						openPopup(GWTClientLogDeobfuscator.getMessage());
					}

				} catch (IOException | IllegalStateException ex) {
					LOGGER.log(Level.SEVERE, ex.getMessage(), ex);
					openPopup(ex.getMessage());
				}
			}
		});
	}

	private void openPopup(String message) {
		JOptionPane.showConfirmDialog(this, message, "Error", JOptionPane.DEFAULT_OPTION);
	}

	private void instantiateComponents() {
		browseLogFile = new JButton();
		browseLogFileLabel = new JLabel();
		titleLabel = new JLabel();
		browseSymbolMap = new JButton();
		browseWarFileLabel = new JLabel();
		browseOutputPathDirectoryLabel = new JLabel();
		browseWarFile = new JButton();
		localeLabel = new JLabel();
		webBrowserLabel = new JLabel();
		browseSymbolMapLabel = new JLabel();
		browseOutputPathDirectory = new JButton();
		localeField = new JTextField();
		webBrowserChoicelist = new JComboBox<>();
		close = new JButton();
		deobfuscate = new JButton();
	}

	private void buildAppLayout() {
		JPanel panel = new JPanel();
		panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

		GridLayout layout = new GridLayout(7, 2);
		layout.setVgap(10);
		layout.setHgap(10);
		panel.setLayout(layout);

		panel.add(browseLogFileLabel);
		panel.add(browseLogFile);

		panel.add(browseSymbolMapLabel);
		panel.add(browseSymbolMap);

		panel.add(browseWarFileLabel);
		panel.add(browseWarFile);

		panel.add(browseOutputPathDirectoryLabel);
		panel.add(browseOutputPathDirectory);

		panel.add(localeLabel);
		panel.add(localeField);

		panel.add(webBrowserLabel);
		panel.add(webBrowserChoicelist);

		panel.add(close);
		panel.add(deobfuscate);

		Container container = getContentPane();
		container.setLayout(new BorderLayout());
		container.add(panel, BorderLayout.CENTER);
	}

	public static void main(String args[]) {
		GWTClientLogDeobfuscatorGUI app = new GWTClientLogDeobfuscatorGUI();
		app.pack();
		app.setVisible(true);
	}
}
