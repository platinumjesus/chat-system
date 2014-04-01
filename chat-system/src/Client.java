import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.Socket;
import java.net.UnknownHostException;

import javax.swing.*;

public class Client extends JFrame {
	private Connection con;
	private String name;
	private final int initialPort = 4001;
	private BufferedReader read;
	private PrintWriter write;
	private boolean clientAccepted;
	private InetAddress host;
	int port;

	public Client() throws UnknownHostException, IOException {
		super();
		this.con = null;
		this.name = null;
		this.read = null;
		this.write = null;
		clientAccepted = true;

		initialize();
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
	 * Initialize the contents of the frame.
	 * 
	 * @throws IOException
	 * @throws UnknownHostException
	 */
	private void initialize() throws UnknownHostException, IOException {

		final JTextArea textArea = new JTextArea(15, 40);
		JTextPane hostname = new JTextPane();
		hostname.setText("localhost");
		JTextPane username = new JTextPane();
		username.setText("Username..");
		JPanel inputPanel = new JPanel();
		inputPanel.add(username);
		inputPanel.add(Box.createHorizontalStrut(15));
		inputPanel.add(hostname);

		int answ = JOptionPane.showConfirmDialog(this, inputPanel,
				"Enter credentials", JOptionPane.YES_NO_OPTION);

		if (answ == JOptionPane.NO_OPTION) {
			System.exit(1);
		}

		else {
			this.setName(username.getText());

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

			Connection con = new Connection(new Socket(host, initialPort));
			this.setCon(con);
			this.setRead(con.createBufferedReader());
			this.setWrite(con.createPrintWriter());

			while (clientAccepted) {
				this.getWrite().println(this.getName());
				System.out.println(this.getName());
				String newport = this.getRead().readLine();

				if (newport != null) {

					System.out.println(newport);
					port = Integer.valueOf(newport);
					try {
						con = new Connection(new Socket(host, port));
						this.setCon(con);
						this.setRead(con.createBufferedReader());
						this.setWrite(con.createPrintWriter());
						clientAccepted = false;
						textArea.append("Welcome "
								+ this.getName()
								+ "you are connected to Hostname: "
								+ con.getNewConnection().getInetAddress()
										.getHostName() + "Port: "
								+ con.getNewConnection().getPort()+"\n");
					} catch (IOException e) {
						System.err
								.println("An error occurred while creating the I/O streams: the socket is closed or it is not connected.");
						e.printStackTrace();
					}
				}
			}
			System.out.println("Connection established.");

			JScrollPane scrollPane = new JScrollPane(textArea);
			scrollPane.setPreferredSize(new Dimension(500, 100));
			textArea.setLineWrap(true);
			textArea.setWrapStyleWord(true);
			textArea.setEditable(false);
			scrollPane
					.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);

			System.out.println("Gui instantiated");

			final JTextField userInputField = new JTextField(40);
			this.setLayout(new FlowLayout());
			this.getContentPane().add(userInputField, SwingConstants.CENTER);
			this.getContentPane().add(scrollPane, SwingConstants.CENTER);

			userInputField.addActionListener(new ActionListener() {

				public void actionPerformed(ActionEvent event) {
					String fromUser = userInputField.getText();

					if (fromUser != null) {
						ServerMessage s = new ServerMessage(fromUser, name, host, port );
						textArea.append(s.toString());
						textArea.setCaretPosition(textArea.getDocument().getLength());
						userInputField.setText("");
					}
				}

			});

			this.setVisible(true);
			this.setSize(500, 170);
			this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
			this.setResizable(false);
			this.setVisible(true);

		}
	}

	public String publish(String message) {
		String newmessage = null;
		return message;
	}

	public Connection getCon() {
		return con;
	}

	public void setCon(Connection con) {
		this.con = con;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public int getInitialPort() {
		return initialPort;
	}

	public BufferedReader getRead() {
		return read;
	}

	public void setRead(BufferedReader read) {
		this.read = read;
	}

	public PrintWriter getWrite() {
		return write;
	}

	public void setWrite(PrintWriter write) {
		this.write = write;
	}

	public boolean isClientAccepted() {
		return clientAccepted;
	}

	public void setClientAccepted(boolean clientAccepted) {
		this.clientAccepted = clientAccepted;
	}

}
