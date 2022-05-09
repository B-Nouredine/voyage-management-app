package package1;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Desktop;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridLayout;
import java.awt.Image;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;
import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;
import javax.swing.table.DefaultTableModel;
import org.h2.tools.DeleteDbFiles;
import com.itextpdf.text.DocumentException;
import com.itextpdf.text.pdf.BaseFont;
import com.itextpdf.text.pdf.PdfContentByte;
import com.itextpdf.text.pdf.PdfReader;
import com.itextpdf.text.pdf.PdfStamper;
import jxl.Sheet;
import jxl.Workbook;
import jxl.read.biff.BiffException;

public class AppBody extends JFrame
{

	/**
	 * 
	 */
	private static final long serialVersionUID = 202108103L;
	JPanel mainPanel, center, north, south, welcomePanel;
	JButton logout, home;
	JMenuBar mainMenu;
	JMenu quotation, voyage, bl;
	
	//QUOTATION VARIABLES
	JPanel createQuotationPanel;
	JMenuItem createQuotation;
	JButton validateQuot, cancelQuot, addCharge;
	//sender, receiver & location information
	JTextField senderEmail, senderPhone, function, to, attention, collectPoint, pol, pod, finalDest, deliveryPoint
	, validity;
	JComboBox<String> incoterms;
	//commodity details
	JTextField packages, goodsDesc, wght, volume;
	JButton addPack;
	ArrayList<String[]> packagesList;
	ArrayList<JButton> removePackage = new ArrayList<JButton>();
	int indexPack = -1 ; //index of buttons in removePackage and packagesList
	//charges (pDialog)
	float usd=0, eur=0; //exchange rates
	JTextField chargeDesc, price, min, qty, exchangeRate; 
	JLabel exchangeLabel;
	JComboBox<String> currency, unit;
	ArrayList<String[]> chargesList;
	JButton addCh;	int indexCh = -1; //index of buttons in removeCharge and charges in chargesList
	ArrayList<JButton> removeCharge = new ArrayList<JButton>();
	
	//VOYAGE VARIABLES
	JPanel createVoyagePanel;
	JMenuItem createVoyage, myVoyages;
	JButton saveVoyage, discardVoyage, search;
	JTextField bookingNb, loadCity, dischargeCity, dateOfLoad, dateOfDischarge, transportingVessel, company,
	commodityTYpe, weight;
	JTextField searchBar;
	JComboBox<String> type;
	JTable voyagesTable;
	String[] columns = {"Booking Number", "Port Of Loading", "Port Of Discharge", "Date Of Load", "Date Of Discharge"
			 ,"Commodity", "Weight", "Trasporting Company", "Vessel Name", "Type"};
	JCheckBox editVoyage;
	JButton removeVoyage;
	
	//BL VARIABLES
	JPanel createBlPanel;
	JMenuItem createBl;
	JTextField bl_Nb, bl_pAcceptance, bl_pLoading, bl_pDischarge, bl_pDelivery, bl_vessel, bl_freight, bl_ref, bl_nbOriginalBl, bl_container, bl_totalPackages, bl_date;
	JTextArea bl_shipper, bl_consignee, bl_notifyAdr, bl_applyTo, bl_charges;
	JButton validateBl, cancelBl, printBl;
	//Details of goods as declared by shipper
	JTextField bl_marksNb, bl_qntyQlty, bl_descriptionGoods, bl_grossWght;
	JButton bl_addItem, addItem, importItems; 
	int indexItem = -1; //index of buttons in removeItem and items in itemsList
	ArrayList<String[]> bl_itemsList = new ArrayList<String[]>();
	ArrayList<String[]> excelItemsList = new ArrayList<String[]>();
	ArrayList<JButton> bl_removeItem = new ArrayList<JButton>();
	
	
	public AppBody(String user) throws Exception { //to be able to know who is the user of the app
	
		//CONNECTING TO THE DATABASE AND CREATING THE REST OF IT
		//we created in the Login class the user table, we'll create the other tables:
		//voyage, vessel, and follow(userId#, voyageId#)
		Connection connection = this.getConnection();
		Statement stat = connection.createStatement();
		
		stat.execute("create table if not exists vessel("
				+	 "vesselId int primary key auto_increment,"
				+ 	 "vesselName varchar(255),"
				+ 	 "CompanyName varchar(255)"
				+ 	 ");");
		
		stat.execute("create table if not exists voyage("
				+ 	 "voyageId Varchar(30) primary key,"
				+ 	 "lCity varchar(255) not null,"
				+ 	 "dCity varchar(255) not null,"						//TO REFERENCE A COLUMN OF A TABLE, WE USE TABLE(COLUMN)
				+ 	 "lDate date not null,"//	RATHER THAN TABLE.COLUMN!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!
				+ 	 "dDate date not null,"
				+  	 "commodity varchar(255),"
				+ 	 "weight float(24),"
				+ 	 "vesselId int references vessel(vesselId) on delete set null," 
				+ 	 "type varchar(6)"
				+ 	 ");");
		
		stat.execute("create table if not exists follow("
				+ 	 "username varchar(255) references sutraUser(username) on delete cascade,"
				+ 	 "voyageId varchar(30) references voyage(voyageId) on delete cascade"
				+ 	 ");");
		
		//#############################
		int X = this.getToolkit().getScreenSize().width;  //SCREEN
		int Y = this.getToolkit().getScreenSize().height; //DIMENSIONS
		//#############################
		this.setTitle("SUTRA: Voyage Management App");
		this.setDefaultCloseOperation(EXIT_ON_CLOSE);
		this.setSize((int) (X), (int) (Y));
		this.setExtendedState(JFrame.MAXIMIZED_BOTH);
		this.setLocationRelativeTo(null);
		//main panel of the app
		mainPanel = new JPanel(new BorderLayout());
		
		welcomePanel = new JPanel();
		ImageIcon imageIcon = new ImageIcon("./images/logo.png");
		java.awt.Image image = imageIcon.getImage(); // transform it 
		java.awt.Image newimg = image.getScaledInstance((int) (X * 0.9), (int) (Y * 0.8),  java.awt.Image.SCALE_SMOOTH); // scale it the smooth way  
		imageIcon = new ImageIcon(newimg);  // transform it back
		welcomePanel.add(new JLabel(imageIcon));
		mainPanel.add(welcomePanel, BorderLayout.CENTER);
		
		imageIcon = new ImageIcon("./images/home.jpg");
		image = imageIcon.getImage(); // transform it 
		newimg = image.getScaledInstance(35, 35,  java.awt.Image.SCALE_SMOOTH); // scale it the smooth way  
		imageIcon = new ImageIcon(newimg);  // transform it back
		home = new JButton("<html><strong>Home &nbsp;&nbsp;</strong></html>" ,imageIcon);
		home.setBackground(Color.green);
		home.setBorder(BorderFactory.createLineBorder(Color.black, 1, false));
		home.setBounds(0, 0, 200, 100);
		home.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent arg0) {
				// TODO Auto-generated method stub
				clearCenter();
				clearWest();
				clearEast();
				mainPanel.add(welcomePanel, BorderLayout.CENTER);
				repaint(); validate();
			}
		});
		
		//*************northern panel*******
		north = new JPanel(new FlowLayout(0,10,10));
		mainPanel.add(north, BorderLayout.NORTH);
		
		//the main menu bar of the app
		mainMenu = new JMenuBar();
		mainMenu.add(Box.createRigidArea(new Dimension(0 ,(int) this.getSize().height/20)));
		mainMenu.setBackground(new Color(220, 220, 240));
		north.add(home);
		north.add(mainMenu);
		//-----
		ImageIcon out = new ImageIcon("./images/logout.png");
		Image logo = out.getImage();
		logo = logo.getScaledInstance(30, 30, Image.SCALE_SMOOTH);
		out = new ImageIcon(logo);
		//-----
		logout = new JButton("Logout", out);
		logout.setBounds(0, 0, 200, 100);
		logout.setBackground(Color.red);
		logout.setForeground(Color.black);
		north.add(logout);
		logout.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent arg0) {
				// TODO Auto-generated method stub
				int response = JOptionPane.showConfirmDialog(SwingUtilities.getWindowAncestor(logout), "Are you sure to logout?");
				if(response==0) // 0:yes/ 1:no/ -1:cancel
				{
					dispose();
					try {
						new Login();
					} catch (Exception e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
			}
		});
		
		//***************the southern panel************
		south = new JPanel(new FlowLayout(1,50,10));
		south.setBackground(new Color(200,200,200));
		south.add(new JLabel("Comapany: SUTRA"));
		south.add(new JLabel("Agency: CASABLANCA"));
		south.add(new JLabel("User: " + user));
		south.add(new JLabel("Logged in at: " + new SimpleDateFormat("dd-MM-yyyy, HH:mm:ss").format(Calendar.getInstance().getTime())));
		mainPanel.add(south, BorderLayout.SOUTH);
		
		//***************the center panel***************
		center = new JPanel();
		
		
		//quotation**************************************
		/////////////////////////////////////////////////
		createQuotationPanel = new JPanel(new BorderLayout());
		JPanel pWest = new JPanel(new GridLayout(0,2));
		JPanel pCenter = new JPanel(new GridLayout(0,2));
		JPanel pDialog = new JPanel(new GridLayout(0,2));
		//pWest
		senderEmail = new JTextField();    
		senderPhone = new JTextField();    
		function = new JTextField();       
		to = new JTextField();             
		attention = new JTextField();      
		collectPoint = new JTextField();   
		pol = new JTextField();            
		pod = new JTextField();            
		finalDest = new JTextField();      
		deliveryPoint = new JTextField(); 
		incoterms = new JComboBox<>(new String[] {"FOB", "EXW", "DAP", "CIF", "CFR", "DDP"});
		validity = new JTextField();
		pWest.add(new JLabel("<html><h2>&nbsp;&nbsp;&nbsp;&nbsp;Sender Information</h2></html>")); pWest.add(Box.createHorizontalGlue());
		pWest.add(new JLabel("Email:")); pWest.add(senderEmail);
		pWest.add(new JLabel("Phone:")); pWest.add(senderPhone);
		pWest.add(new JLabel("Function")); pWest.add(function);
		pWest.add(new JLabel("<html><h2>&nbsp;&nbsp;&nbsp;&nbsp;Receiver Information</h2></html>")); pWest.add(Box.createHorizontalGlue());
		pWest.add(new JLabel("To:")); pWest.add(to);
		pWest.add(new JLabel("Att:")); pWest.add(attention);
		pWest.add(new JLabel("<html><h2>&nbsp;&nbsp;&nbsp;&nbsp;Further Details</h2></html>")); pWest.add(Box.createHorizontalGlue());
		pWest.add(new JLabel("Collection point:")); pWest.add(collectPoint);
		pWest.add(new JLabel("Port of loading:")); pWest.add(pol);
		pWest.add(new JLabel("Port of discharge:")); pWest.add(pod);
		pWest.add(new JLabel("Final destination:")); pWest.add(finalDest);
		pWest.add(new JLabel("Delivery address:")); pWest.add(deliveryPoint);
		pWest.add(new JLabel("Incoterms")); pWest.add(incoterms);
		pWest.add(new JLabel("<html><h2>&nbsp;&nbsp;&nbsp;&nbsp;Validity (of the quotation)</h2></html>")); pWest.add(Box.createHorizontalGlue());
		pWest.add(new JLabel("<html>Valid until (Ex: 01/01/2021) <span style=\"color:red;\">*</span> :</html>")); pWest.add(validity);
		createQuotationPanel.add(pWest, BorderLayout.WEST);
		pWest.setBorder(BorderFactory.createTitledBorder("Identification"));
		//pDialog
		chargeDesc = new JTextField();
		price = new JTextField();
		min = new JTextField();
		qty = new JTextField();
		unit = new JComboBox<>(new String[] {"per W/M", "per 20'", "per 40'", "per expedition"});
		currency = new JComboBox<String>(new String[] {"USD", "EUR", "MAD"});
		exchangeLabel = new JLabel("<html>Enter the current exchange rate (" + currency.getSelectedItem().toString() + " --> MAD)<span style=\"color:red;\">*</span> :</html>");
		exchangeRate = new JTextField();
		addCh = new JButton("Add"); addCh.setBackground(Color.GREEN);
		pDialog.add(new JLabel("<html><h2>&nbsp;&nbsp;&nbsp;&nbsp;Charges & Duties</h2></html>")); pDialog.add(Box.createHorizontalGlue());
		pDialog.add(new JLabel("<html>Type of charge<span style=\"color:red;\">*</span> :</html>")); pDialog.add(chargeDesc);
		pDialog.add(new JLabel("<html>Price<span style=\"color:red;\">*</span> :</html>")); pDialog.add(price);
		pDialog.add(new JLabel("Min:")); pDialog.add(min);
		pDialog.add(new JLabel("Unit:")); pDialog.add(unit);
		pDialog.add(new JLabel("<html>quantity<span style=\"color:red;\">*</span> :</html>")); pDialog.add(qty);
		pDialog.add(new JLabel("Currency:")); pDialog.add(currency);
		pDialog.add(exchangeLabel); pDialog.add(exchangeRate);
		pDialog.add(Box.createHorizontalGlue()); pDialog.add(addCh);
		pDialog.setBorder(BorderFactory.createTitledBorder("About the charges"));
		currency.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent arg0) {
				// TODO Auto-generated method stub
				exchangeLabel.setText("<html>Enter the current exchange rate (" + currency.getSelectedItem().toString() + "-->MAD)<span style=\"color:red;\">*</span> :</html>");
				if(currency.getSelectedItem().toString().equals("MAD"))
				{
					exchangeRate.setText("1"); exchangeRate.setEditable(false);
				}
				else if(currency.getSelectedItem().toString().equals("USD"))
				{
					if(usd==0)
					{
						exchangeRate.setText(""); exchangeRate.setEditable(true);
					}
					else
					{
						exchangeRate.setText(String.valueOf(usd)); exchangeRate.setEditable(false);
					}
				}
				else 
				{
					if(eur==0)
					{
						exchangeRate.setText(""); exchangeRate.setEditable(true);
					}
					else
					{
						exchangeRate.setText(String.valueOf(eur)); exchangeRate.setEditable(false);
					}
				}
				repaint(); validate();
			}
		});
		//pEast
		JPanel pEast = new JPanel(new GridLayout(0,2));
		//pCenter
		JPanel pC = new JPanel(new GridLayout(0,1));
		JPanel pP = new JPanel(new GridLayout(0,1));
		packages = new JTextField();
		goodsDesc = new JTextField();
		wght = new JTextField();
		volume = new JTextField();
		validateQuot = new JButton("Validate"); validateQuot.setBackground(Color.GREEN);
		cancelQuot = new JButton("Cancel"); cancelQuot.setBackground(Color.RED);
		addCharge = new JButton("<html><h2>+</h1>Add new charge</html>"); addCharge.setBackground(Color.BLUE);
		addPack = new JButton("Add package"); addPack.setBackground(Color.CYAN);
		pCenter.add(new JLabel("<html><h2>&nbsp;&nbsp;&nbsp;&nbsp;Commodity Description</h2></html>")); pCenter.add(Box.createHorizontalGlue());
		pCenter.add(new JLabel("<html>Nb & kind of package<span style=\"color:red;\">*</span> :</html>")); pCenter.add(packages);
		pCenter.add(new JLabel("Description:")); pCenter.add(goodsDesc);
		pCenter.add(new JLabel("Weight:")); pCenter.add(wght);
		pCenter.add(new JLabel("Volume:")); pCenter.add(volume);
		pCenter.add(addPack);
		pCenter.add(addCharge);
		pCenter.add(Box.createHorizontalGlue()); pCenter.add(Box.createHorizontalGlue());
		pCenter.add(pP);
		pCenter.add(pC);
		for(int i=0;i<4;i++) pCenter.add(Box.createHorizontalGlue());
		createQuotationPanel.add(pCenter, BorderLayout.CENTER);
		pCenter.setBorder(BorderFactory.createTitledBorder("About the goods"));
		
		for(int i=0;i<8;i++) pEast.add(Box.createHorizontalGlue());
		pEast.add(cancelQuot); pEast.add(validateQuot);
		for(int i=0;i<4;i++) pEast.add(Box.createHorizontalGlue());
		createQuotationPanel.add(pEast, BorderLayout.EAST);
		chargesList = new ArrayList<String[]>();
		addCharge.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent arg0) {
				// TODO Auto-generated method stub
				JDialog D = new JDialog(SwingUtilities.getWindowAncestor(addCharge),"Adding a charge...");
				D.setBounds(200,100,700,400);
				D.add(pDialog);
				D.getRootPane().setDefaultButton(addCh);
				repaint(); validate();
				D.setVisible(true);
			}
		});
		addCh.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent arg0) {
				// TODO Auto-generated method stub
				
				if(indexCh>8)
				{
					JOptionPane.showMessageDialog(null, "Max Charges reached!");
				}else {
				if(chargeDesc.getText().isEmpty() || price.getText().isEmpty() || qty.getText().isEmpty() || exchangeRate.getText().isEmpty())
				{
					JOptionPane.showMessageDialog(null, "<html>Fields with <span style=\"color:red;\">*</span> can't be blank!</html>");
				}
				else
				{
					try {
					float prix = Float.parseFloat(price.getText());
					float qt = Float.parseFloat(qty.getText());
					float total = 0.0f;
					float exchange = Float.parseFloat(exchangeRate.getText());
					float minimum;
					if(!min.getText().isEmpty())
					{
						minimum = Float.parseFloat(min.getText());
					}else minimum = 0;
					
					if(unit.getSelectedItem().toString()!="per expedition") total = Math.max(minimum * exchange, prix * qt * exchange);
					else total = Math.max(minimum * exchange, prix * exchange);
					
					chargesList.add(new String[] {chargeDesc.getText(), price.getText(), min.getText(), 
							unit.getSelectedItem().toString(), qty.getText(), 
							currency.getSelectedItem().toString(), String.valueOf(total)});
					if(currency.getSelectedItem().toString()=="EUR" && eur==0) eur = exchange;
					if(currency.getSelectedItem().toString()=="USD" && usd==0) usd = exchange; 
					
					JButton btn = new JButton(chargeDesc.getText() + " added, remove?");
					removeCharge.add(btn);
					indexCh++;
					removeCharge.get(indexCh).setBackground(Color.BLUE);
					pC.add(removeCharge.get(indexCh));
					currency.setSelectedItem(currency.getSelectedItem());
					chargeDesc.setText(""); //make the text fields empty again for new fill
					price.setText(""); 
					min.setText("");
					qty.setText("");
					
					repaint(); validate();
					removeCharge.get(indexCh).addActionListener(new ActionListener(){
						public void actionPerformed(ActionEvent e)
						{
							JButton b = (JButton) e.getSource();
							int i = removeCharge.indexOf(b);
							chargesList.remove(i);
							pC.remove(removeCharge.get(i));
							removeCharge.remove(i);
							repaint(); validate();
							indexCh--;
						}
					});
					
					}catch(Exception e)
					{
						JOptionPane.showMessageDialog(null, "<html><h3>Verify the entered values<br/>(use <b>.</b> as comma instead of <b>,</b>)</h3></html>");
					}
				}}
			}
		});
		packagesList = new ArrayList<String[]>();
		addPack.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent arg0) {
				// TODO Auto-generated method stub
				if (indexPack > 3)
				{
					JOptionPane.showMessageDialog(null, "Max packages is reached!");
				}else {
				if(!packages.getText().isEmpty())
				{
					packagesList.add(new String[] {packages.getText(), goodsDesc.getText(), wght.getText(), volume.getText()});
					JButton btn = new JButton(packages.getText() + " added, remove?"); btn.setBackground(Color.CYAN);
					removePackage.add(btn);
					indexPack++;
					pP.add(removePackage.get(indexPack));
					packages.setText(""); //make the text fields empty again
					goodsDesc.setText("");
					wght.setText("");
					volume.setText("");
					repaint(); validate();
					removePackage.get(indexPack).addActionListener(new ActionListener() {
						
						@Override
						public void actionPerformed(ActionEvent e) {
							// TODO Auto-generated method stub
							JButton b = (JButton) e.getSource();
							int i = removePackage.indexOf(b);
							packagesList.remove(i);
							pP.remove(removePackage.get(i));
							removePackage.remove(i);
							repaint(); validate();
							indexPack--;
						}
					});
			}else 
			{
				JOptionPane.showMessageDialog(null, "Enter the nb & the kind of the package!");
				packages.setBorder(BorderFactory.createLineBorder(Color.RED));
				packages.requestFocusInWindow();
			}
			}
			}});
		cancelQuot.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent arg0) {
				int response = JOptionPane.showConfirmDialog(SwingUtilities.getWindowAncestor(cancelQuot), "Are you sure to cancel the quotation?");
				if(response == 0)
				{
					//REMOVE ALL THE TEXTS SET
					for(JTextField text: new JTextField[] {chargeDesc, price, min, qty, exchangeRate, 
					packages, goodsDesc, wght, volume, senderEmail, senderPhone, function, 
					to, attention, collectPoint, pol, pod, finalDest, deliveryPoint, validity})
					{
						text.setText("");
					}
					pP.removeAll();
					pC.removeAll();
					repaint(); validate();
					chargesList = new ArrayList<String[]>();
					packagesList = new ArrayList<String[]>();
					removeCharge = new ArrayList<JButton>();
					removePackage = new ArrayList<JButton>();
					indexCh = -1; indexPack = -1;
					home.doClick();
				}
			}
		});
		validateQuot.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent arg0) {
				// TODO Auto-generated method stub
				try {
					Date v = new SimpleDateFormat("dd/MM/yyyy").parse(validity.getText());
					System.out.println(v.toString());
					prepareQuotation(user);
				} catch (ParseException e) {
					// TODO Auto-generated catch block
					JOptionPane.showMessageDialog(null, "Enter a valid validity date!");
					validity.setBorder(BorderFactory.createLineBorder(Color.RED));
					validity.requestFocusInWindow();
				}
			}
		});
		
		
		quotation = new JMenu("   Quotation   ");
		quotation.setBorder(BorderFactory.createLineBorder(new Color(10, 10, 200), 1));
		createQuotation = new JMenuItem("New Quotation");
		createQuotation.setMnemonic(KeyEvent.VK_Q);
		quotation.add(createQuotation);
		mainMenu.add(quotation);
		createQuotation.addMouseListener(new MouseAdapter() {
			@Override
			public void mousePressed(MouseEvent e) {
				// TODO Auto-generated method stub
				super.mousePressed(e);
				clearWest();
				clearEast();
				clearCenter(); //TO REMOVE ONLY THE PANEL IN THE CENTER OF THE mainPanel
				mainPanel.add(createQuotationPanel, BorderLayout.CENTER);
				createQuotationPanel.getRootPane().setDefaultButton(validateQuot);
				repaint(); validate();
			}
		});
		
		//voyage************************************************
		///////////////////////////////////////////////////////
		createVoyagePanel = new JPanel(new GridLayout(0,2));
		bookingNb = new JTextField(); bookingNb.setBorder(BorderFactory.createLineBorder(Color.black, 1, true));
		loadCity = new JTextField(); loadCity.setBorder(BorderFactory.createLineBorder(Color.black, 1, true));
		dischargeCity = new JTextField(); dischargeCity.setBorder(BorderFactory.createLineBorder(Color.black, 1, true));
		dateOfLoad = new JTextField();     dateOfLoad.setBorder(BorderFactory.createLineBorder(Color.black, 1, true));
		dateOfDischarge = new JTextField(); dateOfDischarge.setBorder(BorderFactory.createLineBorder(Color.black, 1, true));
		commodityTYpe = new JTextField();    commodityTYpe.setBorder(BorderFactory.createLineBorder(Color.black, 1, true));
		transportingVessel = new JTextField(); transportingVessel.setBorder(BorderFactory.createLineBorder(Color.black, 1, true));
		weight = new JTextField(); weight.setBorder(BorderFactory.createLineBorder(Color.black, 1, true));
		company = new JTextField(); company.setBorder(BorderFactory.createLineBorder(Color.black, 1, true));
		saveVoyage = new JButton("Save");
		discardVoyage = new JButton("Discard");
		type = new JComboBox<String>(new String[] {"Import", "Export"});
		searchBar = new JTextField(); searchBar.setBorder(BorderFactory.createLineBorder(Color.black, 1, true));
		search = new JButton("Search"); //booking number research
		
		//adding components to the createVoyagePanel
		JPanel p = new JPanel(new FlowLayout(0,50,20));
		p.add(discardVoyage); p.add(saveVoyage);
		createVoyagePanel.add(new JLabel("<html><h3><i>Booking Number: <b><span style=\"color:red;\">*</span></b></i></h3></html>"));
		createVoyagePanel.add(bookingNb);
		createVoyagePanel.add(new JLabel("<html><h3><i>Port Of Loading: <span style=\"color:red;\">*</span></i></h3></html>"));
		createVoyagePanel.add(loadCity);
		createVoyagePanel.add(new JLabel("<html><h3><i>Port Of Discharge: <span style=\"color:red;\">*</span></i></h3></html>"));
		createVoyagePanel.add(dischargeCity);
		createVoyagePanel.add(new JLabel("<html><h3><i>Date Of Loading (Ex: 2021/01/01): <span style=\"color:red;\">*</span></i></h3></html>"));
		createVoyagePanel.add(dateOfLoad);
		createVoyagePanel.add(new JLabel("<html><h3><i>Date Of Discharge (Ex: 2021/01/01): <span style=\"color:red;\">*</span></i></h3></html>"));
		createVoyagePanel.add(dateOfDischarge);
		createVoyagePanel.add(new JLabel("<html><h3><i>Commodity Type:</i></h3></html>"));
		createVoyagePanel.add(commodityTYpe);
		createVoyagePanel.add(new JLabel("<html><h3><i>Weight:</i></h3></html>"));
		createVoyagePanel.add(weight);
		createVoyagePanel.add(new JLabel("<html><h3><i>Shipping Company:</i></h3></html>"));
		createVoyagePanel.add(company);
		createVoyagePanel.add(new JLabel("<html><h3><i>Transporting Vessel (Name):</i></h3></html>"));
		createVoyagePanel.add(transportingVessel);
		createVoyagePanel.add(new JLabel("<html><h3><i>Type Of Expedition: <span style=\"color:red;\">*</span></i></h3></html>"));
		createVoyagePanel.add(type);
		JLabel oblig = new JLabel("<html><b>(<span style=\"color:red;\">*</span>) Obligatory Field</b></html>");
		createVoyagePanel.add(oblig); oblig.setFont(new Font("Arial", Font.ITALIC, 16));
		createVoyagePanel.add(Box.createHorizontalGlue());
		createVoyagePanel.add(Box.createHorizontalGlue());
		createVoyagePanel.add(Box.createHorizontalGlue());
		createVoyagePanel.add(Box.createHorizontalGlue());
		createVoyagePanel.add(p);
		//ActionListeners
		saveVoyage.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent arg0) {
				// TODO Auto-generated method stub
				JTextField[] fields = {bookingNb, loadCity, dischargeCity
						, dateOfLoad, dateOfDischarge};
				boolean thereIsBlank=false;
				JTextField field = null;
				for(JTextField x: fields)
				{
					if(x.getText().isEmpty())
					{
						thereIsBlank = true;
						field = x; break;
					}else x.setBorder(weight.getBorder());
				}
				//if there is at least one obligatory field left blank
				if(thereIsBlank)
				{
					field.setBorder(BorderFactory.createLineBorder(Color.RED));
					JOptionPane.showMessageDialog(null, "<html>Fields with <span style=\"color:red;\">*</span> can't be left blank!</html>");
					field.requestFocusInWindow();
				}
				else
				{
						
					//DATA INSERTION IN THE DATABASE
					Statement st = null;
					boolean bkgNbExits = false;
					try {
						st = connection.createStatement();
						ResultSet rs = st.executeQuery("select * from voyage;");
						while(rs.next())
						{
							if(bookingNb.getText().equals(rs.getString(1)))
							{
								bkgNbExits = true;
								break;
							}
						}
						if(bkgNbExits)
						{ 
							JOptionPane.showMessageDialog(null, "This Booking Number already exists! "
							+ "\nyou can modify the voyage if needed.");
							bookingNb.setBorder(BorderFactory.createLineBorder(Color.RED));
						}
						else
						{
							st = connection.createStatement();
							st.execute("insert into vessel(vesselName, companyName) values("
									+ "'" + transportingVessel.getText() + "',"
									+ "'" + company.getText() + "'"
									+ ");");
							
							 ResultSet res = stat.executeQuery("select max(vesselId) from vessel;");
							 res.next();
							 int Id = res.getInt(1);
							 
							 double w = 0;
							 try {
							 if(!weight.getText().isEmpty())
							 {
							 w = Double.parseDouble(weight.getText());
							 try {
									st.execute("insert into voyage(voyageid,lCity,dCity,lDate,dDate,commodity,weight,vesselId,type) values("
											+ "'" + bookingNb.getText() + "',"
											+ "'" + loadCity.getText() + "',"
											+ "'" + dischargeCity.getText() + "',"
											+ "to_date('" + dateOfLoad.getText() + "', 'yyyy/MM/dd'),"
											+ "to_date('" + dateOfDischarge.getText() + "', 'yyyy/MM/dd'),"
											+ "'" + commodityTYpe.getText() + "',"
										    + w + ","
											+ Id + ","
											+ "'" + type.getSelectedItem().toString() + "'"
											+ ");");
									 }catch(Exception e)
									 { 
										 JOptionPane.showMessageDialog(null, "Invalid date! Pay attention to the date format.");
										 dateOfLoad.setBorder(BorderFactory.createLineBorder(Color.RED));
										dateOfDischarge.setBorder(BorderFactory.createLineBorder(Color.RED));
									 }
							 }}catch(Exception e) { JOptionPane.showMessageDialog(null, "Enter a valid weight");}
							 
							st.execute("insert into follow(username, voyageId) values("
									+ "'" + user + "',"
									+ "'" + bookingNb.getText() + "'"
									+ ");");
							JOptionPane.showMessageDialog(null, "your Expedition has been successfully saved.");
							//clear text fields ==========
							bookingNb.setText(""); loadCity.setText(""); dischargeCity.setText(""); dateOfLoad.setText(""); 
							dateOfDischarge.setText(""); commodityTYpe.setText(""); weight.setText("");
							transportingVessel.setText(""); company.setText("");
							//====================================================
						}
					} catch (SQLException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
			}
		});
		discardVoyage.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent arg0) {
				// TODO Auto-generated method stub
				JTextField[] fields = {bookingNb, loadCity, dischargeCity, dateOfLoad, dateOfDischarge, 
						commodityTYpe, weight, company, transportingVessel};
				boolean allBlank = true;
				
				for(JTextField field: fields)
				{
					if(!field.getText().isEmpty())
					{
						allBlank = false;
						break;
					}
				}
				if(allBlank) home.doClick();
				else
				{
				int a = JOptionPane.showConfirmDialog(null, "Are you sure to discard?");
					if(a==0) 
					{
						for(JTextField field: fields)
						{
							field.setText("");  //clear all texts
						}
						home.doClick();
					}
				}
			}
		});
		
		voyage = new JMenu("   Expedition   ");
		voyage.setBorder(BorderFactory.createLineBorder(new Color(10, 10, 200), 1));
		createVoyage = new JMenuItem("New Expedition");
		createVoyage.setMnemonic(KeyEvent.VK_V);
		createVoyage.addMouseListener(new MouseAdapter() {
			@Override
			public void mousePressed(MouseEvent e) {
				// TODO Auto-generated method stub
				super.mousePressed(e);
				clearWest();
				clearEast();
				clearCenter(); //TO REMOVE ONLY THE PANEL IN THE CENTER OF THE mainPanel
				mainPanel.add(createVoyagePanel, BorderLayout.CENTER);
				createVoyagePanel.getRootPane().setDefaultButton(saveVoyage);
				repaint(); validate();
			}
		});
		//---------------------------------my voyages----------
		editVoyage = new JCheckBox("Edit Expeditions");
		DefaultTableModel model = new DefaultTableModel(columns, 0)
		{
			/**
			 * 
			 */
			private static final long serialVersionUID = 1L;

			public boolean isCellEditable(int row, int column) {
				if(editVoyage.isSelected())
				return true;
				else
				return false;
			};
		};
		voyagesTable = new JTable(model);
		myVoyages = new JMenuItem("My Expeditions");
		myVoyages.setMnemonic(KeyEvent.VK_M);
		myVoyages.addActionListener(new ActionListener() {
		//SHOW DATA ABOUT MY VOYAGES
			@Override
			public void actionPerformed(ActionEvent arg0) {
				// TODO Auto-generated method stub
				try {
					model.setRowCount(0);
					JPanel p = new JPanel(new GridLayout(0,1));
					voyagesTable.setRowHeight(50);
					Statement st = connection.createStatement();	
					ResultSet res = st.executeQuery("select * from voyage, vessel where (voyageId in (select voyageId from follow where username='" + user + "') and voyage.vesselId=vessel.vesselId);");
					
					while(res.next()) 
					{
						String wght = (res.getFloat(7)==0) ? "" : String.valueOf(res.getFloat(7));
						model.addRow(new String[] {res.getString(1), res.getString(2), res.getString(3), res.getString(4), res.getString(5), res.getString(6)
								, wght, res.getString(12), res.getString(11), res.getString(9)});
					}
					JScrollPane sp = new JScrollPane(voyagesTable);
					clearWest();
					clearCenter();
					clearEast();
					//^^^^^^^^^^^RESIZING THE ICON BEFORE SETTING IT^^^^^^^^^^^^^^^^^^^^^^^^^^
					ImageIcon imageIcon = new ImageIcon("./images/search.png"); // load the image to a imageIcon
					java.awt.Image image = imageIcon.getImage(); // transform it 
					java.awt.Image newimg = image.getScaledInstance(20, 20,  java.awt.Image.SCALE_SMOOTH); // scale it the smooth way  
					imageIcon = new ImageIcon(newimg);  // transform it back
					//^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^
					JLabel l = new JLabel("<html><strong>&nbsp;&nbsp;Enter a keyword to search&nbsp;&nbsp;</strong></html>");
					l.setIcon(imageIcon);
					p.add(l);
					p.add(searchBar);
					p.add(new JLabel("<html><div style=\"color:green;\">&nbsp;Note: the search is NOT case sensitive.&nbsp;</div><html>"));
					p.add(Box.createHorizontalGlue());
					p.add(search);
					for(int i=0;i<8;i++)
					{
						p.add(Box.createHorizontalGlue());
					}
					p.add(editVoyage);
					p.add(removeVoyage);
					for(int i=0;i<6;i++)
					{
						p.add(Box.createHorizontalGlue());
					}
					mainPanel.add(p, BorderLayout.EAST);
					p.getRootPane().setDefaultButton(search);
					mainPanel.add(sp, BorderLayout.CENTER);
					repaint(); validate();
				} catch (SQLException e) {
					e.printStackTrace();
				}
			}
		});
		search.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
			if(editVoyage.isSelected()) JOptionPane.showMessageDialog(null, "Cannot search while editing mode is enabled!");
			else {
				if(!searchBar.getText().isEmpty())
				{
					DefaultTableModel model = (DefaultTableModel) voyagesTable.getModel();
					model.setRowCount(0); //THIS IS ABLE TO DELETE ALL THE DATA FROM THE TABLE
					try {
						String toSearch = searchBar.getText();
						Statement st;
						st = connection.createStatement();
						ResultSet res = st.executeQuery("select * from voyage, vessel where voyage.voyageId in "
								+ "(select voyageId from follow where username='" + user +"') and (voyage.voyageId ilike"
								+ " '%" + toSearch + "%' or voyage.lCity ilike '%" + toSearch + "%' or"
								+ " voyage.dCity ilike '%" + toSearch + "%' or voyage.lDate like '%" + toSearch + "%' or"
								+ " voyage.dDate like '%" + toSearch + "%' or voyage.commodity ilike '%" + toSearch + "%'"
								+ " or voyage.weight like '%" + toSearch + "%' or voyage.type ilike '%" + toSearch + "%'"
								+ " or vessel.companyName ilike '%" + toSearch + "%' or"
								+ " vessel.vesselName ilike '%" + toSearch + "%') "
								+ "and voyage.vesselId=vessel.vesselId;");
						
						while(res.next())
						{	
							String wght = (res.getFloat(7)==0) ? "" : String.valueOf(res.getFloat(7));
							model.addRow(new String[] {res.getString(1), res.getString(2), res.getString(3), res.getString(4), res.getString(5), res.getString(6)
										, wght, res.getString(12), res.getString(11), res.getString(9)});
						}
						clearCenter();
						mainPanel.add(new JScrollPane(voyagesTable), BorderLayout.CENTER);
						
						///////////////////////////////////////////////////////////// BEAUTIFUL SOLUTION //////
						//////////////##############################################///////////////////////////
						//HIGHLIGHT THE WORD IN SEARCH WHEREVER FOUND IN THE TABLE****ignoring case sensitivity while searching
						for(int i=0;i<model.getRowCount();i++)
						{
							for(int j=0;j<model.getColumnCount();j++) //##### HTML IS THE SOLUTION #######
							{
								String s = (String) model.getValueAt(i, j);
								if(s.toLowerCase().contains(toSearch.toLowerCase()))
								{
									int indexBefore = s.toLowerCase().indexOf(toSearch.toLowerCase());
									
									String before = s.substring(0, indexBefore);
									String between = s.substring(indexBefore, indexBefore + toSearch.length());
									String after = s.substring(indexBefore + toSearch.length());
									model.setValueAt("<html>" + before + "<span style='background-color: yellow;'>" + between + "</span>" + after + "</html>", i, j);
								}
							}
						}
						//*************************************************************#
						///////////////////////////////////////////////////////////////#
						///////////////////////////////////////////////////////////////#
						
						repaint(); validate();
					} catch (SQLException e1) {
						e1.printStackTrace();
					}
				}
				else
				{
					myVoyages.doClick();
				}
			  }
			}
		});
		//-------------remove voyage------------------------------
		//setting the button's icon
		ImageIcon trash = new ImageIcon("./images/trash.png");
		Image img = trash.getImage();
		img = img.getScaledInstance(30, 30, Image.SCALE_SMOOTH);
		trash = new ImageIcon(img);
		//-------------------------
		removeVoyage = new JButton("Remove Selected Expedition", trash);
		removeVoyage.setBackground(Color.RED); removeVoyage.setForeground(Color.white);
		removeVoyage.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e1) {
				// TODO Auto-generated method stub
				int row = voyagesTable.getSelectedRow();
				if(row>=0)
				{
					int x = JOptionPane.showConfirmDialog(null, "<html><h3>The Expedition whose Booking number is <b><u>" + model.getValueAt(voyagesTable.getSelectedRow(), 0) + "</u></b> will be deleted! Do you confirm?</h3></html>");
					if(x==0)
					{
						try {
							Statement st = connection.createStatement();
							String bkk = model.getValueAt(voyagesTable.getSelectedRow(), 0).toString();
							st.execute("delete from voyage where voyage.voyageId = '" + bkk + "';");
							st.execute("delete from vessel where vessel.vesselId=(select vesselId from voyage where voyage.voyageId='" + bkk + "');");
							myVoyages.doClick();
							JOptionPane.showMessageDialog(null,  "<html><h3>Expedition deleted.</h3></html>");
						} catch (SQLException e) {
							// TODO Auto-generated catch block
							JOptionPane.showMessageDialog(null, "Expedition could not be deleted!");
							e.printStackTrace();
						}
					}
				}else {JOptionPane.showMessageDialog(null, "No expedition (row) was selected!"); }
			}
		});
		//-----------------------------------------------------------
		model.addTableModelListener(new TableModelListener() { //update the expedition data 
			@Override
			public void tableChanged(TableModelEvent e) {
				// TODO Auto-generated method stub
				
				if(editVoyage.isSelected()) //but update only if this condition is true
				{
					try {
						int row = e.getFirstRow();
						int col = e.getColumn();
						
						Statement st = connection.createStatement();
						switch(col)
						{
						case 0:
								JOptionPane.showMessageDialog(null, "The Booking number can not be changed!");
								break;
							
						case 1:
							st.execute("update voyage set lCity = '" + model.getValueAt(row, col) 
							+ "' where voyage.voyageId = '" + model.getValueAt(row, 0) + "';");
							break;
							
						case 2:
							st.execute("update voyage set dCity = '" + model.getValueAt(row, col) 
							+ "' where voyage.voyageId = '" + model.getValueAt(row, 0) + "';");
							break;
						
						case 3:
							st.execute("update voyage set lDate = to_date('" + model.getValueAt(row, col) 
							+ "', 'yyyy-MM-dd') where voyage.voyageId = '" + model.getValueAt(row, 0) + "';");
							break;
							
						case 4:
							st.execute("update voyage set dDate = to_date('" + model.getValueAt(row, col) 
							+ "', 'yyyy-MM-dd') where voyage.voyageId = '" + model.getValueAt(row, 0) + "';");
							break;
							
						case 5:
							st.execute("update voyage set commodity = '" + model.getValueAt(row, col) 
							+ "' where voyage.voyageId = '" + model.getValueAt(row, 0) + "';");
							break;
						
						case 6:
							Float w;
							if(model.getValueAt(row, col).equals("")) w=0f;
							else w = Float.parseFloat((String) model.getValueAt(row, col));
							st.execute("update voyage set weight = " + w 
							+ " where voyage.voyageId = '" + model.getValueAt(row, 0) + "';");
							break;
							
						case 7:
							st.execute("update vessel set companyName = '" + model.getValueAt(row, col) 
							+ "' where vessel.vesselId = (select vesselId from voyage where voyage.voyageId = '" + model.getValueAt(row, 0) + "');");
							break;
							
						case 8:
							st.execute("update vessel set vesselName = '" + model.getValueAt(row, col) 
							+ "' where vessel.vesselId = (select vesselId from voyage where voyage.voyageId = '" + model.getValueAt(row, 0) + "');");
							break;
							
						case 9:
							st.execute("update voyage set type = '" + model.getValueAt(row, col) 
							+ "' where voyage.voyageId = '" + model.getValueAt(row, 0) + "';");
							break;
						}
					} catch (SQLException e1) {
						JOptionPane.showMessageDialog(null, "A problem occurred! changes couldn't be saved!");
						e1.printStackTrace();
					}catch (NumberFormatException e2)
					{
						JOptionPane.showMessageDialog(null, "Invalid weight! changes couldn't be saved!");
					}
				}
			}
		});
		editVoyage.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent arg0) {
				// TODO Auto-generated method stub
				if(editVoyage.isSelected())
				{
						editVoyage.setText("Editing mode!");
						editVoyage.setForeground(Color.red);
						repaint(); validate();
				}
				else {
					editVoyage.setText("Edit Expeditions");
					editVoyage.setForeground(Color.black);
					repaint(); validate();
				}
			}
		});
		voyage.add(createVoyage);
		voyage.add(myVoyages);
		mainMenu.add(voyage);	
		
		//BL***********************************************************************************
		//////////////////////////********************************/////////////////////////////
		///////////////////////////////////////////////////////////////////////////////////////
		createBlPanel = new JPanel(new BorderLayout());
		JPanel bl_pWest = new JPanel(new GridLayout(0,1));
		bl_pWest.setBorder(BorderFactory.createTitledBorder("#"));
		JPanel bl_pCenter = new JPanel(new GridLayout(0,2));
		bl_pCenter.setBorder(BorderFactory.createTitledBorder("##"));
		JPanel bl_pCenterA = new JPanel(new GridLayout(0,1));
		bl_pCenterA.setBorder(BorderFactory.createTitledBorder(""));
		JPanel bl_pCenterB = new JPanel(new GridLayout(0,1));
		bl_pCenterB.setBorder(BorderFactory.createTitledBorder("Details of Goods"));
		JPanel bl_pEast = new JPanel(new GridLayout(0,1));
		bl_pEast.setBorder(BorderFactory.createTitledBorder("###"));
		JPanel bl_pDialog = new JPanel(new GridLayout(0,2));
		bl_Nb = new JTextField(); 
		bl_pAcceptance = new JTextField();
		bl_pLoading = new JTextField();
		bl_pDischarge = new JTextField();
		bl_pDelivery = new JTextField();
		bl_vessel = new JTextField(); 
		bl_freight = new JTextField();
		bl_ref = new JTextField();
		bl_nbOriginalBl = new JTextField();
		bl_container = new JTextField();
		bl_totalPackages = new JTextField();
		bl_date = new JTextField();
		bl_shipper = new JTextArea(5,35);
		bl_consignee = new JTextArea(5,30); 
		bl_notifyAdr = new JTextArea(5,30); 
		bl_applyTo = new JTextArea(4,40);      
		bl_charges = new JTextArea(10,25);
		validateBl = new JButton("Save Draft BL");
		ImageIcon printer = new ImageIcon("./images/printer.png");
		Image img2 = printer.getImage();
		img2 = img2.getScaledInstance(40, 40, Image.SCALE_SMOOTH);
		printer = new ImageIcon(img2);
		printBl = new JButton("<html><h3><i>Print Original BL</i></h3></html>", printer); printBl.setBackground(new Color(10,5,80)); printBl.setForeground(Color.WHITE);
		cancelBl = new JButton("Cancel");
		bl_marksNb = new JTextField();
		bl_qntyQlty = new JTextField();
		bl_descriptionGoods = new JTextField();
		bl_grossWght = new JTextField();
		bl_addItem = new JButton("<html><h2><strong>+</strong>Add an Item</h2></html>");
		bl_addItem.setBackground(Color.BLUE); bl_addItem.setForeground(Color.WHITE);
		addItem = new JButton("Add"); addItem.setBackground(Color.GREEN);
		ImageIcon Import = new ImageIcon("./images/import.png");
		Image img1 = Import.getImage();
		img1 = img1.getScaledInstance(30, 30, Image.SCALE_SMOOTH);
		Import = new ImageIcon(img1);
		importItems = new JButton("Import Excel File", Import);
		importItems.setBackground(Color.lightGray);
		
		bl_pWest.add(new JLabel("<html>BL number<span style='color:red'>*</span> :</html>")); bl_pWest.add(bl_Nb);
		bl_pWest.add(new JLabel("<html>Number of Original BL<span style='color:red'>*</span> :</html>")); bl_pWest.add(bl_nbOriginalBl);
		bl_pWest.add(new JLabel("<html>Reference<span style='color:red'>*</span> :</html>")); bl_pWest.add(bl_ref);
		bl_pWest.add(new JLabel("<html>Container N<span style='color:red'>*</span> :</html>")); bl_pWest.add(bl_container);
		bl_pWest.add(new JLabel("Place of Acceptance:")); bl_pWest.add(bl_pAcceptance);
		bl_pWest.add(new JLabel("<html>Port of Loading:<span style='color:red'>*</span> :</html>")); bl_pWest.add(bl_pLoading);
		bl_pWest.add(new JLabel("<html>Port of Discharge<span style='color:red'>*</span> :</html>")); bl_pWest.add(bl_pDischarge);
		bl_pWest.add(new JLabel("Place of Delivery:")); bl_pWest.add(bl_pDelivery);
		bl_pWest.add(new JLabel("<html>Vessel<span style='color:red'>*</span> :</html>")); bl_pWest.add(bl_vessel);
		bl_pWest.add(new JLabel("<html>Freight<span style='color:red'>*</span> :</html>")); bl_pWest.add(bl_freight);
		
		bl_pCenterA.add(new JLabel("<html>Shipper<span style='color:red'>*</span> :</html>")); bl_pCenterA.add(new JScrollPane(bl_shipper));
		bl_pCenterA.add(new JLabel("<html>Consignee<span style='color:red'>*</span> :</html>")); bl_pCenterA.add(new JScrollPane(bl_consignee));
		bl_pCenterA.add(new JLabel("<html>Notify Address<span style='color:red'>*</span> :</html>")); bl_pCenterA.add(new JScrollPane(bl_notifyAdr));
		bl_pCenterA.add(new JLabel("<html>For Delivery of the Goods Apply to<span style='color:red'>*</span> :</html>")); bl_pCenterA.add(new JScrollPane(bl_applyTo));
		bl_pCenter.add(bl_pCenterA);
		
		JPanel pan0 = new JPanel(new GridLayout(0, 2));
		pan0.add(bl_addItem);
		pan0.add(importItems);
		for(int i=0;i<6;i++)
		{
			pan0.add(Box.createHorizontalGlue());
		}
		bl_pCenterB.add(pan0);
		JPanel pan = new JPanel(new GridLayout(0, 2));
		bl_pCenterB.add(pan);
		bl_pCenter.add(bl_pCenterB);
		
		bl_pDialog.add(new JLabel("<html>Marks & Numbers<span style='color:red;'>*</span> :</html>")); bl_pDialog.add(bl_marksNb);
		bl_pDialog.add(new JLabel("<html>Quantitiy of Packages (or Nb of Seal)<span style='color:red;'>*</span> :</html>")); bl_pDialog.add(bl_qntyQlty);
		bl_pDialog.add(new JLabel("<html>Description of the Goods<span style='color:red;'>*</span> :</html>")); bl_pDialog.add(bl_descriptionGoods);
		bl_pDialog.add(new JLabel("<html>Gross Weight (Kg)<span style='color:red;'>*</span> :</html>")); bl_pDialog.add(bl_grossWght);
		bl_pDialog.add(Box.createHorizontalGlue()); bl_pDialog.add(addItem);
		
		JPanel P = new JPanel(new GridLayout(0,1));
		P.add(new JLabel("<html>Total Packages N<span style='color:red;'>*</span> :</html>")); P.add(bl_totalPackages);
		P.add(new JLabel("<html>Date of BL<span style='color:red;'>*</span> :</html>")); P.add(bl_date);
		bl_pEast.add(P);
		bl_pEast.add(new JLabel("Freight & Charges (or any note):")); bl_pEast.add(new JScrollPane(bl_charges));
		JPanel pBtn = new JPanel();
		pBtn.add(cancelBl); pBtn.add(validateBl); pBtn.add(Box.createVerticalGlue()); pBtn.add(printBl);
		bl_pEast.add(pBtn); bl_pEast.add(printBl);
		
		createBlPanel.add(bl_pWest, BorderLayout.WEST);
		createBlPanel.add(bl_pCenter, BorderLayout.CENTER);
		createBlPanel.add(bl_pEast, BorderLayout.EAST);
		
		bl = new JMenu("   BL   ");
		bl.setBorder(BorderFactory.createLineBorder(new Color(10, 10, 200), 1));
		createBl = new JMenuItem("New BL");
		createBl.setMnemonic(KeyEvent.VK_B);
		bl.add(createBl);
		mainMenu.add(bl);
		createBl.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent arg0) {
				// TODO Auto-generated method stub
				clearCenter();
				clearEast();
				clearWest();
				mainPanel.add(createBlPanel, BorderLayout.CENTER);
				createBlPanel.getRootPane().setDefaultButton(validateBl);
				repaint(); validate();
			}
		});
		
		bl_addItem.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent arg0) {
				JDialog D = new JDialog(SwingUtilities.getWindowAncestor(addCharge),"Adding an Item...");
				D.setBounds((int) (0.25 * X),(int) (0.35 * Y),(int) (0.5 * X),(int) (0.3 * Y));
				D.add(bl_pDialog);
				D.getRootPane().setDefaultButton(addItem);
				repaint(); validate();
				D.setVisible(true);
			}
		});
		
		addItem.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent arg0) {
				JTextField[] fields = {bl_marksNb, bl_qntyQlty, bl_descriptionGoods, bl_grossWght};
				boolean thereIsBlank = false;
				
				for(JTextField field: fields)
				{
					if(field.getText().isEmpty())
					{
						thereIsBlank = true;
						field.setBorder(BorderFactory.createLineBorder(Color.RED));
					}else field.setBorder(bl_pDelivery.getBorder());
				}
			if(thereIsBlank)
			{
				JOptionPane.showMessageDialog(null, "<html>Fields with <span style='color:red;'>*</span> can't be left blank!</html>");
			}
			else
			{
				if(indexItem>10) //we will determine the number n later n --> n+2 items
				{
					JOptionPane.showMessageDialog(null, "Max Items reached!");
				}else {
					try {
						float test = Float.parseFloat(bl_grossWght.getText());
						System.out.println(test);
						bl_grossWght.setBorder(bl_pDelivery.getBorder());
						bl_itemsList.add(new String[] {bl_marksNb.getText(), bl_qntyQlty.getText(), 
								bl_descriptionGoods.getText().toString(), bl_grossWght.getText()});
						
						JButton btn = new JButton(bl_marksNb.getText() + " added, remove?");
						bl_removeItem.add(btn);
						indexItem++;
						bl_removeItem.get(indexItem).setBackground(Color.RED);
						bl_removeItem.get(indexItem).setForeground(Color.WHITE);
						pan.add(bl_removeItem.get(indexItem));
						bl_marksNb.setText(""); //make the text fields empty again for new fill
						bl_qntyQlty.setText(""); 
						bl_descriptionGoods.setText("");
						bl_grossWght.setText("");
						repaint(); validate();
											
						bl_removeItem.get(indexItem).addActionListener(new ActionListener(){
							public void actionPerformed(ActionEvent e)
							{
								JButton b = (JButton) e.getSource();
								int i = bl_removeItem.indexOf(b);
								bl_itemsList.remove(i);
								pan.remove(bl_removeItem.get(i));
								bl_removeItem.remove(i);
								repaint(); validate();
								indexItem--;
							}
						});
					}catch(NumberFormatException e) {
						JOptionPane.showMessageDialog(null, "<html>Weight not valid!<br/>Try using <b>.</b> instead of <b>,</b></html>");
						bl_grossWght.setBorder(BorderFactory.createLineBorder(Color.RED));
					}
					}}}
		});
		
		importItems.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent arg0) {
				// TODO Auto-generated method stub
					//#################
					importExcelData(pan);//############################################//
					//################
			}
		});
		
		validateBl.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent arg0) {
				JTextField[] fields = new JTextField[] {bl_Nb, bl_pLoading, bl_pDischarge, bl_vessel, 
						bl_freight, bl_ref, bl_nbOriginalBl, bl_container, bl_totalPackages, bl_date};
				JTextArea[] areas = new JTextArea[] {bl_shipper, bl_consignee, bl_notifyAdr, bl_applyTo};
				boolean thereIsBlank = false;
				
				for(JTextField field: fields)
				{
					if(field.getText().isEmpty())
					{
						thereIsBlank = true;
						field.setBorder(BorderFactory.createLineBorder(Color.RED));
					}else field.setBorder(bl_pDelivery.getBorder());
				}
				for(JTextArea area: areas)
				{
					if(area.getText().isEmpty())
					{
						thereIsBlank = true;
						area.setBorder(BorderFactory.createLineBorder(Color.RED));
					}else area.setBorder(bl_pDelivery.getBorder());
				}
				if(thereIsBlank)
				{
					JOptionPane.showMessageDialog(null, "<html>Fields with <span style='color:red;'>*</span> can't be left blank!</html>");
				}
				else
				{
					prepareBl();
				}
			}
		});
		
		printBl.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent arg0) {
				// TODO Auto-generated method stub
				JTextField[] fields = new JTextField[] {bl_Nb, bl_pLoading, bl_pDischarge, bl_vessel, 
						bl_freight, bl_ref, bl_nbOriginalBl, bl_container, bl_totalPackages, bl_date};
				JTextArea[] areas = new JTextArea[] {bl_shipper, bl_consignee, bl_notifyAdr, bl_applyTo };
				boolean thereIsBlank = false;
				
				for(JTextField field: fields)
				{
					if(field.getText().isEmpty())
					{
						thereIsBlank = true;
						field.setBorder(BorderFactory.createLineBorder(Color.RED));
					}else field.setBorder(bl_pDelivery.getBorder());
				}
				for(JTextArea area: areas)
				{
					if(area.getText().isEmpty())
					{
						thereIsBlank = true;
						area.setBorder(BorderFactory.createLineBorder(Color.RED));
					}else area.setBorder(bl_pDelivery.getBorder());
				}
				if(thereIsBlank)
				{
					JOptionPane.showMessageDialog(null, "<html>Fields with <span style='color:red;'>*</span> can't be left blank!</html>");
				}
				else
				{
				preparePrintableBl();
				}
			}
		});
		
		cancelBl.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent arg0) {
				int i = JOptionPane.showConfirmDialog(null, "Are you sure to cancel the BL?");
				if(i==0)
				{
					//CLEAR ALL THE TEXTS
					for(JTextField text: new JTextField[] {bl_Nb, bl_pAcceptance, bl_pLoading, 
					bl_pDischarge, bl_pDelivery, bl_vessel, bl_freight, bl_ref, bl_nbOriginalBl, 
					bl_container, bl_totalPackages, bl_date, bl_marksNb, bl_qntyQlty, 
					bl_descriptionGoods, bl_grossWght})
					{
						text.setText("");
					}
					for(JTextArea text: new JTextArea[] {bl_shipper, bl_consignee, bl_notifyAdr, 
							bl_applyTo, bl_charges})
					{
						text.setText("");
					}
					importItems.setText("Import Excel File"); importItems.setBackground(Color.LIGHT_GRAY);
					//REMOVE ALL BUTTONS AND ITEMS
					pan.removeAll();
					repaint(); validate();
					bl_removeItem = new ArrayList<JButton>();
					bl_itemsList = new ArrayList<String[]>();
					excelItemsList = new ArrayList<String[]>();
					indexItem = -1;
					//RETURN TO HOME
					home.doClick();
				}
			}
		});
		
		this.setContentPane(mainPanel);
		this.setVisible(true);
	} //here ends the constructor

	//methods
	private void clearCenter() //TO REMOVE ONLY THE PANEL IN THE CENTER OF THE mainPanel
	{
		BorderLayout layout = (BorderLayout) mainPanel.getLayout(); 
		if(mainPanel.isAncestorOf(layout.getLayoutComponent(BorderLayout.CENTER))) //IF EXISTS
		{
			mainPanel.remove(layout.getLayoutComponent(BorderLayout.CENTER));
		}
	}
	
	private void clearEast()
	{
		BorderLayout layout = (BorderLayout) mainPanel.getLayout(); 
		if(mainPanel.isAncestorOf(layout.getLayoutComponent(BorderLayout.EAST))) //IF EXISTS
		{
			mainPanel.remove(layout.getLayoutComponent(BorderLayout.EAST));
		}
	}
	
	private void clearWest()
	{
		BorderLayout layout = (BorderLayout) mainPanel.getLayout(); 
		if(mainPanel.isAncestorOf(layout.getLayoutComponent(BorderLayout.WEST))) //IF EXISTS
		{
			mainPanel.remove(layout.getLayoutComponent(BorderLayout.WEST));
		}
	}

	private Connection getConnection() throws Exception {
	    DeleteDbFiles.execute("~", "test", true);
	    
	    Class.forName("org.h2.Driver");
	    try { Connection conn = DriverManager.getConnection("jdbc:h2:~/sutra","root","pass");
	    	return conn;	    	
	    }catch (Exception e) {
	        e.printStackTrace();
	        return null;
	    }
	}
	
	private void prepareQuotation(String user)
	{
		try {
			PdfReader reader = new PdfReader("./docs/standardQuot.pdf");
			JFileChooser jfc = new JFileChooser();
			int a = jfc.showSaveDialog(validateQuot);
			
			if(a == JFileChooser.APPROVE_OPTION && jfc.getSelectedFile()!=null)
			{
					String extension = "";

					if(!jfc.getSelectedFile().getAbsolutePath().endsWith(".pdf"))
					{
						extension = ".pdf";
					}
					PdfStamper stamper = new PdfStamper(reader, new FileOutputStream(jfc.getSelectedFile().getAbsolutePath() + extension));

			BaseFont baseFont = BaseFont.createFont(
	                BaseFont.TIMES_BOLD, 
	                BaseFont.CP1250, BaseFont.NOT_EMBEDDED);
			
			PdfContentByte stylo = stamper.getOverContent(1);
			stylo.beginText();
			
			//insertion des donnees
			//identification
			stylo.setFontAndSize(baseFont, 11);
			stylo.setRGBColorFill(0, 0, 0);
			stylo.setTextMatrix(370, 736);
			stylo.showText(new SimpleDateFormat("dd/MM/yyyy").format(Calendar.getInstance().getTime())); //date
			stylo.setTextMatrix(370, 720);
			stylo.showText(to.getText()); // To:
			stylo.setTextMatrix(370, 700);
			stylo.showText(attention.getText()); // Att:
			stylo.setTextMatrix(370, 665);
			stylo.showText(user); //Sender's name:
			stylo.setTextMatrix(370, 640);
			stylo.showText(senderEmail.getText()); //Sender's email
			stylo.setTextMatrix(370, 615);
			stylo.showText(senderPhone.getText());       //Sender's phone
			stylo.setTextMatrix(370, 588);
			stylo.showText(function.getText());          //sender's function
			//locations
			stylo.setFontAndSize(baseFont, 9);
			stylo.setTextMatrix(270, 501);
			stylo.showText(collectPoint.getText()); //collection Point
			stylo.setTextMatrix(270, 491);
			stylo.showText(pol.getText()); //pol
			stylo.setTextMatrix(270, 480);
			stylo.showText(pod.getText()); //pod
			stylo.setTextMatrix(480, 500);
			stylo.showText(finalDest.getText()); //final Destination
			String s = deliveryPoint.getText();
			int pos = 18;
			if(s.length()>18)
			{
				for(int i=0;i<=18;i++)
				{
					if(s.toCharArray()[i]==' ')
					{
						pos = i;
					}
				}
				stylo.setTextMatrix(480, 489);
				stylo.showText(s.substring(0, pos));           //Delivery Point
				stylo.setTextMatrix(389, 478); 
				stylo.showText(s.substring(pos+1, s.length())); //Delivery Point, continue
			}
			else
			{
				stylo.setTextMatrix(480, 489);
			stylo.showText(deliveryPoint.getText());			//Delivery Point
			}
			//packages
			for(int i=0;i<packagesList.size();i++) {
			stylo.setTextMatrix(175, 433-11*i);
			stylo.showText(packagesList.get(i)[0]); //nb and kind of packs
			stylo.setTextMatrix(290, 433-11*i);
			stylo.showText(packagesList.get(i)[1]);     //desc
			stylo.setTextMatrix(390, 433-11*i);
			stylo.showText(packagesList.get(i)[2]);     //weight
			stylo.setTextMatrix(490, 433-11*i);
			stylo.showText(packagesList.get(i)[3]);     //volume
			}
			//incoterms
			stylo.setFontAndSize(baseFont, 11);
			stylo.setTextMatrix(180, 372);
			stylo.showText("INCOTERM: " + incoterms.getSelectedItem().toString());
			//date validity
			stylo.setRGBColorFill(255, 0, 0);
			stylo.setTextMatrix(500, 372);
			stylo.showText(validity.getText());     //valid until
			//rates (charges)
			stylo.setFontAndSize(baseFont, 9);
			stylo.setRGBColorFill(0, 0, 0);
			for(int i=0;i<chargesList.size();i++)
			{
			stylo.setTextMatrix(30, 283-15*i);
			stylo.showText(chargesList.get(i)[0]); //charge
			stylo.setTextMatrix(172, 283-15*i);
			stylo.showText(chargesList.get(i)[1]); //price
			stylo.setTextMatrix(220, 283-15*i);
			stylo.showText((chargesList.get(i)[2].equals("")) ? "" : ("/   " + chargesList.get(i)[2]));
			stylo.setTextMatrix(300, 283-15*i);
			stylo.showText(chargesList.get(i)[3]); //unit
			stylo.setTextMatrix(400, 283-15*i);
			stylo.showText(chargesList.get(i)[4]); //qty
			stylo.setTextMatrix(465, 283-15*i);
			stylo.showText(chargesList.get(i)[5]); //currency
			stylo.setTextMatrix(505, 283-15*i);
			stylo.showText(chargesList.get(i)[6]); //total
			stylo.setTextMatrix(552, 283-15*i);
			stylo.showText("MAD");
			}
			//total of total (Final Amount)
			stylo.setRGBColorFill(20, 20, 150);
			stylo.setTextMatrix(510, 112);
			float total = 0;
			for(int j=0;j<chargesList.size();j++)
			{
				total += Float.parseFloat(chargesList.get(j)[6]);
			}
			stylo.showText(String.valueOf(total) + " MAD");
			//Note
			stylo.setFontAndSize(baseFont, 11);
			stylo.setRGBColorFill(0, 0, 0);
			String note = (usd==0 && eur==0)? "" : "Note: all totals are in MAD considering the current exchange rates: [";
			String line = "";
			note += (usd==0)? "" : "  USD = " + usd +" MAD";
			note += (eur==0)? "" : "  EUR= " + eur + " MAD";
			note += (note.equals("")) ? "" : " ].";
			stylo.setTextMatrix(25, 84);
			stylo.showText(note);
			for(int i=0;i<note.length();i++)
			{
				line += "----";
			}
			stylo.setTextMatrix(0, 94); //line above
			stylo.showText(line);
			stylo.setTextMatrix(0, 74); //line below
			stylo.showText(line);
			
			stylo.endText();
			stamper.close();
			Desktop.getDesktop().open(new File(jfc.getSelectedFile().getAbsolutePath()+extension));
			}else return;
		} catch (IOException | DocumentException e1) {
			// TODO Auto-generated catch block
			JOptionPane.showMessageDialog(null, "Coudn't register the quotation!");
			e1.printStackTrace();
		}
	}
	
	private void prepareBl()
	{
		try {
			PdfReader reader = new PdfReader("./docs/standardBl.pdf");
			JFileChooser jfc = new JFileChooser();
			
			int a = jfc.showSaveDialog(validateBl);
			String extension = "";
			
			if(a == JFileChooser.APPROVE_OPTION && jfc.getSelectedFile()!=null)
			{
				if(!jfc.getSelectedFile().getAbsolutePath().endsWith(".pdf"))
				{
					extension = ".pdf";
				}
				
				PdfStamper stamper = new PdfStamper(reader, new FileOutputStream(jfc.getSelectedFile().getAbsolutePath() + extension));
				PdfContentByte stylo = stamper.getOverContent(1);
				BaseFont courier = BaseFont.createFont(BaseFont.COURIER_BOLDOBLIQUE, BaseFont.CP1252, BaseFont.NOT_EMBEDDED);
				stylo.beginText();
								
				//id
				stylo.setFontAndSize(courier, 16); //FONT 18
				stylo.setTextMatrix(390, 744);
				stylo.showText(bl_Nb.getText()); //Bl number
				//parts info
				String[] shpr = bl_shipper.getText().split("\\n");
				stylo.setFontAndSize(courier, 8); //FONT 10
				for(int i=0;i<shpr.length;i++)
				{
					if(i==0) stylo.setTextMatrix(60, 764 - 12*i);
					else stylo.setTextMatrix(20, 764 - 12*i);
				stylo.showText(shpr[i]); //shipper
				}
				String[] cnee = bl_consignee.getText().split("\\n");
				for(int i=0;i<cnee.length;i++)
				{
					if(i==0) stylo.setTextMatrix(70, 700 - 12*i);
					else stylo.setTextMatrix(20, 700 - 12*i);
				stylo.showText(cnee[i]); //consignee
				}
				String[] ntify = bl_notifyAdr.getText().split("\\n");
				for(int i=0;i<ntify.length;i++)
				{
					if(i==0) stylo.setTextMatrix(85, 635 - 12*i);
					else stylo.setTextMatrix(20, 635 - 12*i);
				stylo.showText(ntify[i]);  //notify address
				}
				String[] appto = bl_applyTo.getText().split("\\n");
				for(int i=0;i<appto.length;i++)
				{
				stylo.setTextMatrix(20, 545 - 12*i);
				stylo.showText(appto[i]); //apply to
				}
				stylo.setTextMatrix(310, 545);
				stylo.showText(bl_nbOriginalBl.getText());		 //nb of original bl
				stylo.setTextMatrix(450, 545);
				stylo.showText(bl_ref.getText());      //REFERENCE
				stylo.setTextMatrix(320, 510);
				stylo.showText(bl_container.getText()); //CONTAINER NB
				//places 
				stylo.setTextMatrix(20, 480);
				stylo.showText(bl_pAcceptance.getText());     //PLACE OF ACCEPTANCE
				stylo.setTextMatrix(220, 480);
				stylo.showText(bl_pLoading.getText());     //PORT OF LOADING
				stylo.setTextMatrix(442, 480);
				stylo.showText(bl_vessel.getText()); 		//VESSEL
				stylo.setTextMatrix(20, 448);
				stylo.showText(bl_pDischarge.getText());    //PORT OF DISCHARGE
				stylo.setTextMatrix(220, 448);
				stylo.showText(bl_pDelivery.getText());   //PLACE OF DELIVERY
				stylo.setTextMatrix(442, 448);
				stylo.showText(bl_freight.getText());  		//FREIGHT
				//DETAILS OF THE GOODS
				int distance = 0;
				//inserted items
				float Total = 0f;
				for(int i=0;i<bl_itemsList.size();i++)
				{
				stylo.setTextMatrix(20, 385 - 12*i);
				stylo.showText(bl_itemsList.get(i)[0]);	//MARKS AND NB
				stylo.setTextMatrix(138, 385 - 12*i);
				stylo.showText(bl_itemsList.get(i)[1]);	//QNTY & QLTY
				stylo.setTextMatrix(270, 385 - 12*i);
				stylo.showText(bl_itemsList.get(i)[2]);   //DESC OF GOODS
				if(bl_itemsList.get(i)[3]!=null)
				{
					try {
							Total+=Float.parseFloat(bl_itemsList.get(i)[3]);
						}catch(NumberFormatException e)
						{
							e.printStackTrace();
							JOptionPane.showMessageDialog(null, "one of gross weights is probably not a number!");
						}
				}
				stylo.setTextMatrix(455, 385 -12*i);
				stylo.showText(bl_itemsList.get(i)[3]);	//GROSS WEIGHT
				}
				//imported items
				int j = bl_itemsList.size();
				for(int i=0;i<excelItemsList.size();i++)
				{
				stylo.setTextMatrix(20, 385 - 12*(i+j));
				stylo.showText(excelItemsList.get(i)[0]);	//MARKS AND NB
				stylo.setTextMatrix(138, 385 - 12*(i+j));
				stylo.showText(excelItemsList.get(i)[1]);	//QNTY & QLTY
				stylo.setTextMatrix(270, 385 - 12*(i+j));
				stylo.showText(excelItemsList.get(i)[2]);   //DESC OF GOODS
				if(excelItemsList.get(i)[3]!=null)
				{
					try {
					Total+=Float.parseFloat(excelItemsList.get(i)[3]);
					}catch(NumberFormatException e)
					{
						e.printStackTrace();
						JOptionPane.showMessageDialog(null, "one of gross weights is probably not a number!");
					}
				}
				stylo.setTextMatrix(455, 385 -12*(i+j));
				stylo.showText(excelItemsList.get(i)[3]);	//GROSS WEIGHT
				}
				distance = excelItemsList.size()+j;
				//TOTAL
				if(Total!=0f)
				{
				String line=""; for(int i=0;i<31;i++) line+="---";
				stylo.setTextMatrix(30, 385 -12*distance);
				stylo.showText(line);	//line
				stylo.setFontAndSize(courier, 12);
				stylo.setTextMatrix(380, 385 -12*distance-20);
				stylo.showText("Total:");
				stylo.setTextMatrix(455, 385 -12*distance-20);
				stylo.showText(String.valueOf(Total));
				}
				//notes, date, and place
				String[] notes = bl_charges.getText().split("\\n");
				for(int i=0;i<notes.length;i++)
				{
				stylo.setTextMatrix(20, 180 - 12*i);
				stylo.showText(notes[i]);  //FREIGHTS AND CHARGES
				}
				stylo.setTextMatrix(300, 89);
				stylo.showText(bl_totalPackages.getText());  //TOTAL PACKAGES
				stylo.setTextMatrix(270, 75);
				stylo.showText("CASABLANCA");	//ISSUED AT
				stylo.setTextMatrix(385, 75);
				stylo.showText(bl_date.getText());	//ON THE <date>
								
				stylo.endText();
				stamper.close();
				Desktop.getDesktop().open(new File(jfc.getSelectedFile().getAbsolutePath()+extension));
			}else return;

		} catch (IOException | DocumentException e) {
			JOptionPane.showMessageDialog(null, "Coudn't register the BL!");
			e.printStackTrace();
		}
	}
	
	private void importExcelData(JPanel pan)
	{
		JFileChooser jfc = new JFileChooser();
		int a = jfc.showSaveDialog(importItems);
		
//		jfc.addChoosableFileFilter(new FileNameExtensionFilter(".xls"));
		if(a==JFileChooser.APPROVE_OPTION && jfc.getSelectedFile()!=null)
		{			
			try {
				Workbook wb = Workbook.getWorkbook(new File(jfc.getSelectedFile().getAbsolutePath()));
				Sheet sheet = wb.getSheet(0);
				int rows = sheet.getRows();
				int start = 1;   //we start reading from the second row considering that the first contains
								// only fields names, but if it doesn't then we start with the first
				
					String wght = "0123456789";
					for(int i=0;i<10;i++)
					{
						if(sheet.getCell(3, 0).getContents().contains(wght.subSequence(i, i+1)))
						{
							start = 0;
							break;
						}
					}
					rows = Integer.min(rows, 12 + start); //10 rows maximum are allowed 
				
				for(int i=start;i<rows;i++)
				{
					String w = sheet.getCell(3,i).getContents();
					if(w.contains(","))
					{
						w = w.replace(',', '.'); // to match float
					}
					excelItemsList.add(new String[] {sheet.getCell(0,i).getContents(), sheet.getCell(1,i).getContents(),
							sheet.getCell(2,i).getContents(), w});
				}
				
				importItems.setText(jfc.getSelectedFile().getName());
				importItems.setBackground(Color.GREEN);
				JOptionPane.showMessageDialog(null, "<html><h2 style='color:green;'>Data imported successfully!</h2></html>");
				repaint(); validate();
				
				wb.close();
			} catch (BiffException | IOException e) {
				JOptionPane.showMessageDialog(null, "Something went wrong with your imported file! Retry again.");
				e.printStackTrace();
			}
		}else return;
	}
	
	private void preparePrintableBl()
	{
		try {
				float header = 47.24f;
				PdfReader reader = new PdfReader("./docs/emptyBl.pdf");
				
				PdfStamper stamper = new PdfStamper(reader, new FileOutputStream("./docs/printableBl.pdf"));
				
				PdfContentByte stylo = stamper.getOverContent(1);
				BaseFont courier = BaseFont.createFont(BaseFont.COURIER_BOLDOBLIQUE, BaseFont.CP1252, BaseFont.NOT_EMBEDDED);
				stylo.beginText();
								
				//id
				stylo.setFontAndSize(courier, 16); //FONT 18
				stylo.setTextMatrix(390, 744 + header);
				stylo.showText(bl_Nb.getText()); //Bl number
				//parts info
				String[] shpr = bl_shipper.getText().split("\\n");
				stylo.setFontAndSize(courier, 8); //FONT 10
				for(int i=0;i<shpr.length;i++)
				{
					if(i==0) stylo.setTextMatrix(60, 764 - 12*i + header);
					else stylo.setTextMatrix(20, 764 - 12*i + header);
				stylo.showText(shpr[i]); //shipper
				}
				String[] cnee = bl_consignee.getText().split("\\n");
				for(int i=0;i<cnee.length;i++)
				{
					if(i==0) stylo.setTextMatrix(70, 700 - 12*i + header);
					else stylo.setTextMatrix(20, 700 - 12*i + header);
				stylo.showText(cnee[i]); //consignee
				}
				String[] ntify = bl_notifyAdr.getText().split("\\n");
				for(int i=0;i<ntify.length;i++)
				{
					if(i==0) stylo.setTextMatrix(85, 635 - 12*i + header);
					else stylo.setTextMatrix(20, 635 - 12*i + header);
				stylo.showText(ntify[i]);  //notify address
				}
				String[] appto = bl_applyTo.getText().split("\\n");
				for(int i=0;i<appto.length;i++)
				{
				stylo.setTextMatrix(20, 545 - 12*i + header);
				stylo.showText(appto[i]); //apply to
				}
				stylo.setTextMatrix(310, 545 + header);
				stylo.showText(bl_nbOriginalBl.getText());		 //nb of original bl
				stylo.setTextMatrix(450, 545 + header);
				stylo.showText(bl_ref.getText());      //REFERENCE
				stylo.setTextMatrix(320, 510 + header);
				stylo.showText(bl_container.getText()); //CONTAINER NB
				//places 
				stylo.setTextMatrix(20, 480 + header);
				stylo.showText(bl_pAcceptance.getText());     //PLACE OF ACCEPTANCE
				stylo.setTextMatrix(220, 480 + header);
				stylo.showText(bl_pLoading.getText());     //PORT OF LOADING
				stylo.setTextMatrix(442, 480 + header);
				stylo.showText(bl_vessel.getText()); 		//VESSEL
				stylo.setTextMatrix(20, 448 + header);
				stylo.showText(bl_pDischarge.getText());    //PORT OF DISCHARGE
				stylo.setTextMatrix(220, 448 + header);
				stylo.showText(bl_pDelivery.getText());   //PLACE OF DELIVERY
				stylo.setTextMatrix(442, 448 + header);
				stylo.showText(bl_freight.getText());  		//FREIGHT
				//DETAILS OF THE GOODS
				int distance = 0;
				//inserted items
				float Total = 0f;
				for(int i=0;i<bl_itemsList.size();i++)
				{
				stylo.setTextMatrix(20, 385 - 12*i + header);
				stylo.showText(bl_itemsList.get(i)[0]);	//MARKS AND NB
				stylo.setTextMatrix(138, 385 - 12*i + header);
				stylo.showText(bl_itemsList.get(i)[1]);	//QNTY & QLTY
				stylo.setTextMatrix(270, 385 - 12*i + header);
				stylo.showText(bl_itemsList.get(i)[2]);   //DESC OF GOODS
				if(bl_itemsList.get(i)[3]!=null)
				{
					try {
							Total+=Float.parseFloat(bl_itemsList.get(i)[3]);
						}catch(NumberFormatException e)
						{
							e.printStackTrace();
							JOptionPane.showMessageDialog(null, "one of gross weights is probably not a number!");
						}
				}
				stylo.setTextMatrix(455, 385 -12*i + header);
				stylo.showText(bl_itemsList.get(i)[3]);	//GROSS WEIGHT
				}
				//imported items
				int j = bl_itemsList.size();
				for(int i=0;i<excelItemsList.size();i++)
				{
				stylo.setTextMatrix(20, 385 - 12*(i+j) + header);
				stylo.showText(excelItemsList.get(i)[0]);	//MARKS AND NB
				stylo.setTextMatrix(138, 385 - 12*(i+j) + header);
				stylo.showText(excelItemsList.get(i)[1]);	//QNTY & QLTY
				stylo.setTextMatrix(270, 385 - 12*(i+j) + header);
				stylo.showText(excelItemsList.get(i)[2]);   //DESC OF GOODS
				if(excelItemsList.get(i)[3]!=null)
				{
					try {
					Total+=Float.parseFloat(excelItemsList.get(i)[3]);
					}catch(NumberFormatException e)
					{
						e.printStackTrace();
						JOptionPane.showMessageDialog(null, "one of gross weights is probably not a number!");
					}
				}
				stylo.setTextMatrix(455, 385 -12*(i+j) + header);
				stylo.showText(excelItemsList.get(i)[3]);	//GROSS WEIGHT
				}
				distance =  excelItemsList.size()+j;
				//TOTAL
				if(Total!=0f)
				{
				String line=""; for(int i=0;i<31;i++) line+="---";
				stylo.setTextMatrix(30, 385 -12*distance + header);
				stylo.showText(line);	//line
				stylo.setFontAndSize(courier, 12);
				stylo.setTextMatrix(380, 385 -12*distance-20 + header);
				stylo.showText("Total:");
				stylo.setTextMatrix(455, 385 -12*distance-20 + header);
				stylo.showText(String.valueOf(Total));
				}
				//notes, date, and place
				String[] notes = bl_charges.getText().split("\\n");
				for(int i=0;i<notes.length;i++)
				{
				stylo.setTextMatrix(20, 180 - 12*i + header);
				stylo.showText(notes[i]);  //FREIGHTS AND CHARGES
				}
				stylo.setTextMatrix(300, 89 + header);
				stylo.showText(bl_totalPackages.getText());  //TOTAL PACKAGES
				stylo.setTextMatrix(270, 75 + header);
				stylo.showText("CASABLANCA");	//ISSUED AT
				stylo.setTextMatrix(385, 75 + header);
				stylo.showText(bl_date.getText());	//ON THE <date>
								
				stylo.endText();
				stamper.close();
				Desktop.getDesktop().open(new File("./docs/printableBl.pdf"));
				
		} catch (IOException | DocumentException e) {
			JOptionPane.showMessageDialog(null, "Coudn't register the BL!");
			e.printStackTrace();
		}
	}
	
}
