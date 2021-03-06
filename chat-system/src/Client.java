import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.InetAddress;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.ArrayList;

import javax.swing.Box;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.JTextPane;
import javax.swing.ScrollPaneConstants;
import javax.swing.SwingConstants;
import javax.swing.UIManager;
/**
 * The Client class implements the Client side feature in a separate 
 * execution from the Server class:
 * -A thread that continuously listen to the Server messages;
 * -Another thread that continuously send the user input to the Server.
 * 
 * @author Ettore Ciprian
 * @author Marco Zanellati
 * @author Tobias Bernard
 *
 */
public class Client extends JFrame implements Runnable{
	private static final long serialVersionUID = -5507269416865711567L;
	/**
	 * Implementation of GUI and functionalities of a Client
	 */
	private Connection con;
	private String name;
	private final int port = 4001;
	private boolean clientAccepted;
	private InetAddress host;
	private ArrayList<String> history;
	private JTextArea textArea = new JTextArea(25, 80);
	private JTextField userInputField = new JTextField(53);
	
	private DataInputStream in;
	private DataOutputStream out;

	public Client() throws UnknownHostException, IOException {
		super();
		this.con = null;
		this.name = null;
		clientAccepted = false;
		initialize();
		new Thread(this).start();
	}

	/**
	 * Launch the application.
	 * 
	 * @throws IOException
	 * @throws UnknownHostException
	 */
	public static void main(String[] args) throws UnknownHostException,
			IOException {

		new Client();
	}

	/**
	 * Initialize the elements of the frame and ask for host and username.
	 * 
	 * @throws IOException
	 * @throws UnknownHostException
	 */
	private void initialize() throws UnknownHostException, IOException {
		 try {
	           UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
	       } catch (Exception e) {
	           System.out.println("Unable to set native look and feel: " + e);
	       }
		history = new ArrayList<String>();

		JTextPane hostname = new JTextPane();
		hostname.setText("localhost");
		JTextPane username = new JTextPane();
		username.setText("Username..");
		hostname.setPreferredSize(new Dimension(150, 20));
		username.setPreferredSize(new Dimension(150, 20));
		JPanel inputPanel = new JPanel();
		inputPanel.add(username);
		inputPanel.add(Box.createHorizontalStrut(20));
		inputPanel.add(hostname);
		inputPanel.setPreferredSize(new Dimension(350, 40));

		// At initialization, ask the user for username and host.
		int answ = JOptionPane.showConfirmDialog(this, inputPanel,
				"Enter credentials", JOptionPane.YES_NO_OPTION);

		if (answ == JOptionPane.NO_OPTION) {
			System.exit(1);
		}

		else {

			setName(username.getText().replaceAll("\n", ""));

			if (hostname.getText().isEmpty()) {
				JOptionPane.showMessageDialog(null, hostname.getText()
						+ " is not a valid IP address.");
				System.exit(1);
			} else {
				try {
					host = InetAddress.getByName(hostname.getText());
				} catch (Exception e) {
					JOptionPane.showMessageDialog(null, host.toString()
							+ " is not correct");
					System.err
							.println("An error occurred while looking up for the Host.");
					e.printStackTrace();
					System.exit(1);

				}
				if (!host.isReachable(20)) {
					JOptionPane.showMessageDialog(null, host.toString()
							+ " is not reachable");
					System.exit(1);
				}

			}

			// Handshake with Server succeeded
			if (host != null) {
				try {
					con = new Connection(new Socket(host, port));
					this.setCon(con);
					clientAccepted = true;
					textArea.append("Welcome "
							+ this.getName()
							+ " you are connected to \nHostname: "
							+ con.getNewConnection().getInetAddress()
									.getHostName()
							+ "\nPort: "
							+ con.getNewConnection().getPort()
							+ "\n"
							+ "--------------------------------------------------------\n");
				} catch (IOException e) {
					System.err
							.println("An error occurred while creating the I/O streams: the socket is closed or it is not connected.");
					e.printStackTrace();
				}
			}
		}
		System.out.println("Connection established.");
		
		in = con.createBufferedReader();
		out = con.createPrintWriter();
		
		JScrollPane scrollPane = new JScrollPane(textArea);
		scrollPane.setPreferredSize(new Dimension(585, 150));
		textArea.setLineWrap(true);
		textArea.setWrapStyleWord(true);
		textArea.setEditable(false);
		scrollPane
				.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);

		System.out.println("GUI instantiated");

		this.setLayout(new FlowLayout());
		this.getContentPane().add(userInputField, SwingConstants.CENTER);
		this.getContentPane().add(scrollPane, SwingConstants.CENTER);

		
		userInputField.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent event) {
				String fromUser = userInputField.getText();

				if (fromUser != null) {
					Message s = new Message(fromUser, name, host, port);
					// textArea.append(s.toString());
					synchronized (history) {
						try {
							out.writeUTF(s.toString());
						} catch (IOException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
						textArea.setCaretPosition(textArea.getDocument()
								.getLength());
						userInputField.setText("");
						history.add(s.toString());
					}
				}
			}

		});

		this.setVisible(true);
		this.setSize(600, 225);
		this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		this.setResizable(false);

	}

	/**
	 * Receive messages
	 */
	@Override
	public void run() {
		while (true) {
			try {
				String fromServer = in.readUTF();
				if (fromServer != null) {

					synchronized (history) {
						if (history.contains(fromServer)) {
							textArea.append("         Me: ");
							textArea.append(fromServer.split("\\[")[2]
									.split("\\]")[0] + "\n");
							// textArea.append(fromServer + "\n");
							textArea.setCaretPosition(textArea.getDocument()
									.getLength());
							userInputField.setText("");
						} else {
							textArea.append(fromServer.split("\\[")[1]
									.split("\\]")[0]
									+ ": "
									+ fromServer.split("\\[")[2].split("\\]")[0]
									+ "\n");
							// textArea.append(fromServer.toString() + "\n");
							textArea.setCaretPosition(textArea.getDocument()
									.getLength());
							userInputField.setText("");
						}
					}
				}
			} catch (IOException e) {
				System.out.println("[ERROR] " + host
						+ " no longer available. Closing..");
				System.exit(-1);

			}
		}
	}

	// Getters and Setters
	public Connection getCon() {
		return con;
	}

	public void setCon(Connection con) {
		this.con = con;
	}

	@Override
	public String getName() {
		return name;
	}

	@Override
	public void setName(String name) {
		this.name = name;
	}

	public boolean isClientAccepted() {
		return clientAccepted;
	}

	public void setClientAccepted(boolean clientAccepted) {
		this.clientAccepted = clientAccepted;
	}

}
