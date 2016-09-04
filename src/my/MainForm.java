/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package my;

import java.awt.Color;
import java.awt.TrayIcon;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import javax.swing.filechooser.FileNameExtensionFilter;
import miLibreria.*;
import miLibreria.bd.ManejoBDAccess;
import miLibreria.bd.ManejoBDI;
import miLibreria.bd.ManejoBDSqlServer;
import miLibreria.bd.Usuarios;

/**
 *
 * @author Luis
 */
public class MainForm extends javax.swing.JFrame {

    /**
     * Creates new form MainForm
     */
    public Rpt001 oRpt001;
    public Rpt002 oRpt002;
    public Rpt003 oRpt003;
    public Rpt004 oRpt004;
    public Rpt005 oRpt005;
    public static String sDir;
    public ManejoBDI oBD;
    public static String msgVersion="";
    public String ldap="";
    public final int NINGUNO=0,ADMINISTRADOR=1,ACTUALIZADOR=2,REPORTEADOR=3;
    public long nivel=NINGUNO;
    Usuarios oUsuarios=new Usuarios(); 
    public final int MSACCESS=1, SQLSERVER=2;
    public int tipoConn=MSACCESS;
    
    private BDConn jBDConn;
    
    private String tipoConeccion;
    private String ubicacion;
    private String servidor;
    private String instancia;
    private String userId;
    private String pass;
    private boolean esWindows;
    private String bd;
    
    public MainForm() {
        initComponents();
        tipoConn=MSACCESS;
        msgVersion="Project Leader: Luis Fernando Perez Armas\nLArmas@slb.com\n\nProgramming: LAP Consultores C.A. \nTlf: 58-414-9337812\n\nVersion: 1.0\nOctober 2015\n\n";
        this.setLocationRelativeTo(null);
                
        jBDConn=new BDConn(this,true);
        jBDConn.llenarPantalla(jBDConn.sParam);         
        jBDConn.setVisible(true);        
        tipoConeccion=jBDConn.getTipoConeccion();
        ubicacion=jBDConn.getUbicacion();
        servidor=jBDConn.getServidor();
        instancia=jBDConn.getInstancia();
        userId=jBDConn.getUserId();
        pass=jBDConn.getPassWord();
        esWindows=jBDConn.getEsWindows();
        bd=jBDConn.getBD();
        
        if ("MSACCESS".equals(tipoConeccion)) {
           oBD=new ManejoBDAccess();
           oBD.setUbicacion(ubicacion);
        }
        else {
           oBD=new ManejoBDSqlServer();
           oBD.setServidor(servidor);
           oBD.setInstancia(instancia);
           oBD.setBD(bd);
           oBD.setUserId(userId);
           oBD.setPassword(pass);
           oBD.setEsWindows(esWindows);
        }
        
        oBD.conectar();
        if (oBD.getconectado()==false) try {
            System.exit(0);
        } catch (Throwable ex) {
            Logger.getLogger(MainForm.class.getName()).log(Level.SEVERE, null, ex);
        }
        ldap=System.getProperty("user.name");
        
        if ("LArmas".equals(ldap) || "Luis".equals(ldap) || "USUARIO".equals(ldap)) {
            nivel=ADMINISTRADOR;
        } else {
            try {
                Object[] o= oBD.select(Usuarios.class,"ldap='"+ldap+"'");
                if (o.length>0) {
                   oUsuarios=(Usuarios) o[0];
                   nivel=desencriptarNivel(ldap,(int) oUsuarios.getNivel());
                }
            } catch (Exception ex) {
                Logger.getLogger(MainForm.class.getName()).log(Level.SEVERE, null, ex);
                return;
            }
            if (nivel==NINGUNO) { 
                msgbox("Usted no se encuentra registrado en el sistema.");
                System.exit(0);
            }
        }
        
        oRpt001 = new Rpt001(this,true,oBD);
        oRpt002 = new Rpt002(this,true,oBD);
        oRpt003 = new Rpt003(this,true,oBD); 
        oRpt004 = new Rpt004(this,true,oBD);
        oRpt005 = new Rpt005(this,true,oBD);
        oRpt001.setLocationRelativeTo(this);
        oRpt002.setLocationRelativeTo(this);
        oRpt003.setLocationRelativeTo(this);
        oRpt004.setLocationRelativeTo(this);
        oRpt005.setLocationRelativeTo(this);
 
        jLabelReporte001.setForeground(Color.BLACK);
        jLabelReporte002.setForeground(Color.BLACK);
        jLabelReporte003.setForeground(Color.BLACK);
        jLabelReporte004.setForeground(Color.BLACK);
        jLabelReporte005.setForeground(Color.BLACK);
    }
    
    public static boolean mdIguales(double md1, double md2) {
        boolean ok=false;
        if (Math.abs(md2-md1)<0.1) ok=true;
        return ok;
    }
    
    public void msgbox(String s, String t){
        JOptionPane.showMessageDialog(null, s,t,TrayIcon.MessageType.WARNING.ordinal());        
    }
    
    private void llamaRpt001() {
        oRpt001.setVisible(true);
    }
    
    private void llamaRpt002() {
        oRpt002.setVisible(true);
    }
    
    private void llamaRpt003() {
        oRpt003.setVisible(true);
    }
    
    private void llamaRpt004() {
        oRpt004.setVisible(true);
    }  
    
    private void llamaRpt005() {
        oRpt005.setVisible(true);
    } 
    
    public void leerConfig() {
        File currDirectory=null, selectedFile=null;
        currDirectory=new File("config.001");
        
        Writer writer = null;
      
        
        if (!currDirectory.exists()) {
            try {
                writer = new BufferedWriter(new OutputStreamWriter(
                      new FileOutputStream("config.001"), "utf-8"));
                writer.write("MSACCESS");
                writer.write("c:\\");
                
            } catch (IOException ex) {
              // report
            } finally {
               try {writer.close();} catch (Exception ex) {/*ignore*/}
            }
        } 
        String line = null;
        BufferedReader in = null;

        try {
           in = new BufferedReader(new FileReader("config.001"));
        } catch (FileNotFoundException ex) {
           Logger.getLogger(MainForm.class.getName()).log(Level.SEVERE, null, ex);
        }
        try {
           while ((line = in.readLine()) != null) {
               if ("MSACCESS".equals(line.trim())) {
                   tipoConn=MSACCESS;
               }
               if ("MSACCESS".equals(line.trim())) {
                   tipoConn=MSACCESS;
               }
           }
           in.close();
        } catch (IOException ex) {
           Logger.getLogger(MainForm.class.getName()).log(Level.SEVERE, null, ex);
        }

        currDirectory=new File(line);
    }
    
    public String seleccionaArchivo(){
        File currDirectory=null, selectedFile=null;
        currDirectory=new File("config.001");
        
        Writer writer = null;
      
        
        if (!currDirectory.exists()) {
            try {
                writer = new BufferedWriter(new OutputStreamWriter(
                      new FileOutputStream("config.001"), "utf-8"));
                writer.write("c:\\");
            } catch (IOException ex) {
              // report
            } finally {
               try {writer.close();} catch (Exception ex) {/*ignore*/}
            }
        }
        
        String line = null;
        BufferedReader in = null;

        try {
           in = new BufferedReader(new FileReader("config.001"));
        } catch (FileNotFoundException ex) {
           Logger.getLogger(MainForm.class.getName()).log(Level.SEVERE, null, ex);
        }
        try {
           line = in.readLine();
           in.close();
        } catch (IOException ex) {
           Logger.getLogger(MainForm.class.getName()).log(Level.SEVERE, null, ex);
        }

        currDirectory=new File(line);
       
        String s="";
        JFileChooser fileChooser = new JFileChooser();
        FileNameExtensionFilter filter = new FileNameExtensionFilter("Base de datos Access", new String[] {"accdb"});
        fileChooser.setCurrentDirectory(currDirectory);
        fileChooser.setDialogTitle("Escoja la ubicacion de la base de datos");
        fileChooser.setFileFilter(null);      
        fileChooser.setFileFilter(filter);
        fileChooser.setAcceptAllFileFilterUsed(false);
        fileChooser.addChoosableFileFilter(filter); 
        int result = fileChooser.showOpenDialog(this);
        currDirectory=fileChooser.getCurrentDirectory();
        if (result == JFileChooser.APPROVE_OPTION) {
            selectedFile = fileChooser.getSelectedFile();
            s=selectedFile.toString();
            try {
                writer = new BufferedWriter(new OutputStreamWriter(
                      new FileOutputStream("config.001"), "utf-8"));
                writer.write(s);
            } catch (IOException ex) {
              // report
            } finally {
               try {writer.close();} catch (Exception ex) {/*ignore*/}
            }
        }
        return s;
    }
    
    private int encriptarNivel(String s,int i) {
        int nivelEncriptado=0;
        int j=0;
        if (i==1) j=9748;
        if (i==2) j=15623;
        if (i==3) j=3276;
        nivelEncriptado=s.charAt(0)+s.charAt(3)+s.charAt(2)+j;
        return nivelEncriptado;
    }
    
    private int desencriptarNivel(String s,int i) {
        int nivelDesencriptado=0;
        int j=0;
        j=i-s.charAt(0)-s.charAt(3)-s.charAt(2);
        if (j==9748) nivelDesencriptado=1;
        if (j==15623) nivelDesencriptado=2;
        if (j==3276) nivelDesencriptado=3;
        return nivelDesencriptado;        
    }
    
    public void msgbox(String s){
        JOptionPane.showMessageDialog(null, s);        
    }
    
    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        buttonGroup1 = new javax.swing.ButtonGroup();
        jLabel9 = new javax.swing.JLabel();
        jLabelReporte002 = new javax.swing.JLabel();
        jLabelReporte001 = new javax.swing.JLabel();
        jLabelReporte003 = new javax.swing.JLabel();
        jLabelReporte004 = new javax.swing.JLabel();
        jLabelReporte005 = new javax.swing.JLabel();
        jRadioButtonDirectionalDrillers = new javax.swing.JRadioButton();
        jRadioButtonDrillinglEngineers = new javax.swing.JRadioButton();
        jScrollPane1 = new javax.swing.JScrollPane();
        jTextPane1 = new javax.swing.JTextPane();
        jLabel1 = new javax.swing.JLabel();

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);
        setTitle("WellDataReport");
        setMinimumSize(new java.awt.Dimension(610, 460));
        setResizable(false);
        getContentPane().setLayout(new org.netbeans.lib.awtextra.AbsoluteLayout());

        jLabel9.setIcon(new javax.swing.ImageIcon(getClass().getResource("/my/SB.png"))); // NOI18N
        jLabel9.setBorder(javax.swing.BorderFactory.createEtchedBorder());
        jLabel9.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                jLabel9MouseClicked(evt);
            }
        });
        getContentPane().add(jLabel9, new org.netbeans.lib.awtextra.AbsoluteConstraints(460, 20, 140, 40));

        jLabelReporte002.setFont(new java.awt.Font("Tahoma", 1, 18)); // NOI18N
        jLabelReporte002.setText("Well Planning Offset Data");
        jLabelReporte002.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        jLabelReporte002.setEnabled(false);
        jLabelReporte002.addFocusListener(new java.awt.event.FocusAdapter() {
            public void focusLost(java.awt.event.FocusEvent evt) {
                jLabelReporte002FocusLost(evt);
            }
            public void focusGained(java.awt.event.FocusEvent evt) {
                jLabelReporte002FocusGained(evt);
            }
        });
        jLabelReporte002.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                jLabelReporte002MouseClicked(evt);
            }
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                jLabelReporte002MouseEntered(evt);
            }
            public void mouseExited(java.awt.event.MouseEvent evt) {
                jLabelReporte002MouseExited(evt);
            }
        });
        getContentPane().add(jLabelReporte002, new org.netbeans.lib.awtextra.AbsoluteConstraints(310, 190, 250, 20));

        jLabelReporte001.setFont(new java.awt.Font("Tahoma", 1, 18)); // NOI18N
        jLabelReporte001.setText("Well Excecution Data");
        jLabelReporte001.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        jLabelReporte001.setEnabled(false);
        jLabelReporte001.addFocusListener(new java.awt.event.FocusAdapter() {
            public void focusLost(java.awt.event.FocusEvent evt) {
                jLabelReporte001FocusLost(evt);
            }
            public void focusGained(java.awt.event.FocusEvent evt) {
                jLabelReporte001FocusGained(evt);
            }
        });
        jLabelReporte001.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                jLabelReporte001MouseClicked(evt);
            }
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                jLabelReporte001MouseEntered(evt);
            }
            public void mouseExited(java.awt.event.MouseEvent evt) {
                jLabelReporte001MouseExited(evt);
            }
        });
        getContentPane().add(jLabelReporte001, new org.netbeans.lib.awtextra.AbsoluteConstraints(310, 150, 200, 20));

        jLabelReporte003.setFont(new java.awt.Font("Tahoma", 1, 18)); // NOI18N
        jLabelReporte003.setText("Well Summary");
        jLabelReporte003.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        jLabelReporte003.setEnabled(false);
        jLabelReporte003.addFocusListener(new java.awt.event.FocusAdapter() {
            public void focusLost(java.awt.event.FocusEvent evt) {
                jLabelReporte003FocusLost(evt);
            }
            public void focusGained(java.awt.event.FocusEvent evt) {
                jLabelReporte003FocusGained(evt);
            }
        });
        jLabelReporte003.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                jLabelReporte003MouseClicked(evt);
            }
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                jLabelReporte003MouseEntered(evt);
            }
            public void mouseExited(java.awt.event.MouseEvent evt) {
                jLabelReporte003MouseExited(evt);
            }
        });
        getContentPane().add(jLabelReporte003, new org.netbeans.lib.awtextra.AbsoluteConstraints(310, 230, 200, 20));

        jLabelReporte004.setFont(new java.awt.Font("Tahoma", 1, 18)); // NOI18N
        jLabelReporte004.setText("Well Planning risk analysis");
        jLabelReporte004.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        jLabelReporte004.setEnabled(false);
        jLabelReporte004.addFocusListener(new java.awt.event.FocusAdapter() {
            public void focusLost(java.awt.event.FocusEvent evt) {
                jLabelReporte004FocusLost(evt);
            }
            public void focusGained(java.awt.event.FocusEvent evt) {
                jLabelReporte004FocusGained(evt);
            }
        });
        jLabelReporte004.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                jLabelReporte004MouseClicked(evt);
            }
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                jLabelReporte004MouseEntered(evt);
            }
            public void mouseExited(java.awt.event.MouseEvent evt) {
                jLabelReporte004MouseExited(evt);
            }
        });
        getContentPane().add(jLabelReporte004, new org.netbeans.lib.awtextra.AbsoluteConstraints(310, 270, 250, 20));

        jLabelReporte005.setFont(new java.awt.Font("Tahoma", 1, 18)); // NOI18N
        jLabelReporte005.setText("Field Summary Report");
        jLabelReporte005.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        jLabelReporte005.setEnabled(false);
        jLabelReporte005.addFocusListener(new java.awt.event.FocusAdapter() {
            public void focusLost(java.awt.event.FocusEvent evt) {
                jLabelReporte005FocusLost(evt);
            }
            public void focusGained(java.awt.event.FocusEvent evt) {
                jLabelReporte005FocusGained(evt);
            }
        });
        jLabelReporte005.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                jLabelReporte005MouseClicked(evt);
            }
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                jLabelReporte005MouseEntered(evt);
            }
            public void mouseExited(java.awt.event.MouseEvent evt) {
                jLabelReporte005MouseExited(evt);
            }
        });
        getContentPane().add(jLabelReporte005, new org.netbeans.lib.awtextra.AbsoluteConstraints(310, 310, 250, 20));

        jRadioButtonDirectionalDrillers.setBackground(new java.awt.Color(255, 255, 255));
        buttonGroup1.add(jRadioButtonDirectionalDrillers);
        jRadioButtonDirectionalDrillers.setFont(new java.awt.Font("Tahoma", 1, 14)); // NOI18N
        jRadioButtonDirectionalDrillers.setForeground(new java.awt.Color(0, 0, 255));
        jRadioButtonDirectionalDrillers.setText("Directional Drillers");
        jRadioButtonDirectionalDrillers.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jRadioButtonDirectionalDrillersActionPerformed(evt);
            }
        });
        getContentPane().add(jRadioButtonDirectionalDrillers, new org.netbeans.lib.awtextra.AbsoluteConstraints(430, 90, 170, 30));

        jRadioButtonDrillinglEngineers.setBackground(new java.awt.Color(255, 255, 255));
        buttonGroup1.add(jRadioButtonDrillinglEngineers);
        jRadioButtonDrillinglEngineers.setFont(new java.awt.Font("Tahoma", 1, 14)); // NOI18N
        jRadioButtonDrillinglEngineers.setForeground(new java.awt.Color(0, 0, 255));
        jRadioButtonDrillinglEngineers.setText("Drilling Engineers ");
        jRadioButtonDrillinglEngineers.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jRadioButtonDrillinglEngineersActionPerformed(evt);
            }
        });
        getContentPane().add(jRadioButtonDrillinglEngineers, new org.netbeans.lib.awtextra.AbsoluteConstraints(250, 90, 170, 30));

        jTextPane1.setEditable(false);
        jTextPane1.setBackground(new java.awt.Color(204, 204, 204));
        jTextPane1.setBorder(javax.swing.BorderFactory.createEtchedBorder());
        jTextPane1.setFont(new java.awt.Font("Tahoma", 0, 10)); // NOI18N
        jTextPane1.setForeground(new java.awt.Color(0, 0, 153));
        jTextPane1.setText("Disclosure: Slide Sheet Analyzer was conceived as a support tool of drilling offset data, which only aims to help directional drillers and drilling engineers during well execution and well design repectively, the application never intends to substitute the criteria and decisions made by directional drillers or drilling engineers.");
        jScrollPane1.setViewportView(jTextPane1);

        getContentPane().add(jScrollPane1, new org.netbeans.lib.awtextra.AbsoluteConstraints(0, 410, 610, 50));

        jLabel1.setIcon(new javax.swing.ImageIcon(getClass().getResource("/my/PowerDrive X6 Signature Illustration.jpg"))); // NOI18N
        getContentPane().add(jLabel1, new org.netbeans.lib.awtextra.AbsoluteConstraints(0, 0, 610, 460));

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void jLabel9MouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_jLabel9MouseClicked
        msgbox(msgVersion,"About WellDataReport");
    }//GEN-LAST:event_jLabel9MouseClicked

    private void jLabelReporte001MouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_jLabelReporte001MouseClicked
        llamaRpt001();
    }//GEN-LAST:event_jLabelReporte001MouseClicked

    private void jLabelReporte001FocusGained(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_jLabelReporte001FocusGained

    }//GEN-LAST:event_jLabelReporte001FocusGained

    private void jLabelReporte001FocusLost(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_jLabelReporte001FocusLost

    }//GEN-LAST:event_jLabelReporte001FocusLost

    private void jLabelReporte001MouseEntered(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_jLabelReporte001MouseEntered
        jLabelReporte001.setForeground(Color.BLUE);
    }//GEN-LAST:event_jLabelReporte001MouseEntered

    private void jLabelReporte001MouseExited(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_jLabelReporte001MouseExited
         jLabelReporte001.setForeground(Color.BLACK);
    }//GEN-LAST:event_jLabelReporte001MouseExited

    private void jLabelReporte002FocusGained(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_jLabelReporte002FocusGained
        // TODO add your handling code here:
    }//GEN-LAST:event_jLabelReporte002FocusGained

    private void jLabelReporte002FocusLost(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_jLabelReporte002FocusLost
        // TODO add your handling code here:
    }//GEN-LAST:event_jLabelReporte002FocusLost

    private void jLabelReporte002MouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_jLabelReporte002MouseClicked
        llamaRpt002();
    }//GEN-LAST:event_jLabelReporte002MouseClicked

    private void jLabelReporte002MouseEntered(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_jLabelReporte002MouseEntered
        jLabelReporte002.setForeground(Color.BLUE);
    }//GEN-LAST:event_jLabelReporte002MouseEntered

    private void jLabelReporte002MouseExited(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_jLabelReporte002MouseExited
        jLabelReporte002.setForeground(Color.BLACK);
    }//GEN-LAST:event_jLabelReporte002MouseExited

    private void jLabelReporte003FocusGained(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_jLabelReporte003FocusGained
        // TODO add your handling code here:
    }//GEN-LAST:event_jLabelReporte003FocusGained

    private void jLabelReporte003FocusLost(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_jLabelReporte003FocusLost
        // TODO add your handling code here:
    }//GEN-LAST:event_jLabelReporte003FocusLost

    private void jLabelReporte003MouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_jLabelReporte003MouseClicked
        llamaRpt003();
    }//GEN-LAST:event_jLabelReporte003MouseClicked

    private void jLabelReporte003MouseEntered(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_jLabelReporte003MouseEntered
        jLabelReporte003.setForeground(Color.BLUE);
    }//GEN-LAST:event_jLabelReporte003MouseEntered

    private void jLabelReporte003MouseExited(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_jLabelReporte003MouseExited
        jLabelReporte003.setForeground(Color.BLACK);
    }//GEN-LAST:event_jLabelReporte003MouseExited

    private void jLabelReporte004FocusGained(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_jLabelReporte004FocusGained
        // TODO add your handling code here:
    }//GEN-LAST:event_jLabelReporte004FocusGained

    private void jLabelReporte004FocusLost(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_jLabelReporte004FocusLost
        // TODO add your handling code here:
    }//GEN-LAST:event_jLabelReporte004FocusLost

    private void jLabelReporte004MouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_jLabelReporte004MouseClicked
        llamaRpt004();
    }//GEN-LAST:event_jLabelReporte004MouseClicked

    private void jLabelReporte004MouseEntered(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_jLabelReporte004MouseEntered
        jLabelReporte004.setForeground(Color.BLUE);
    }//GEN-LAST:event_jLabelReporte004MouseEntered

    private void jLabelReporte004MouseExited(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_jLabelReporte004MouseExited
        jLabelReporte004.setForeground(Color.BLACK);
    }//GEN-LAST:event_jLabelReporte004MouseExited

    private void jLabelReporte005FocusGained(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_jLabelReporte005FocusGained
        // TODO add your handling code here:
    }//GEN-LAST:event_jLabelReporte005FocusGained

    private void jLabelReporte005FocusLost(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_jLabelReporte005FocusLost
        // TODO add your handling code here:
    }//GEN-LAST:event_jLabelReporte005FocusLost

    private void jLabelReporte005MouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_jLabelReporte005MouseClicked
        llamaRpt005();
    }//GEN-LAST:event_jLabelReporte005MouseClicked

    private void jLabelReporte005MouseEntered(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_jLabelReporte005MouseEntered
        jLabelReporte005.setForeground(Color.BLUE);
    }//GEN-LAST:event_jLabelReporte005MouseEntered

    private void jLabelReporte005MouseExited(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_jLabelReporte005MouseExited
        jLabelReporte005.setForeground(Color.BLACK);
    }//GEN-LAST:event_jLabelReporte005MouseExited

    private void jRadioButtonDrillinglEngineersActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jRadioButtonDrillinglEngineersActionPerformed
        this.jLabelReporte001.setEnabled(false);
        this.jLabelReporte002.setEnabled(false);
        this.jLabelReporte003.setEnabled(true);
        this.jLabelReporte004.setEnabled(true);
        this.jLabelReporte005.setEnabled(true);
    }//GEN-LAST:event_jRadioButtonDrillinglEngineersActionPerformed

    private void jRadioButtonDirectionalDrillersActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jRadioButtonDirectionalDrillersActionPerformed
        this.jLabelReporte001.setEnabled(true);
        this.jLabelReporte002.setEnabled(true);
        this.jLabelReporte003.setEnabled(false);
        this.jLabelReporte004.setEnabled(false);
        this.jLabelReporte005.setEnabled(false);
    }//GEN-LAST:event_jRadioButtonDirectionalDrillersActionPerformed

    /**
     * @param args the command line arguments
     */
    public static void main(String args[]) {
        /* Set the Nimbus look and feel */
        //<editor-fold defaultstate="collapsed" desc=" Look and feel setting code (optional) ">
        /* If Nimbus (introduced in Java SE 6) is not available, stay with the default look and feel.
         * For details see http://download.oracle.com/javase/tutorial/uiswing/lookandfeel/plaf.html 
         */
        try {
            for (javax.swing.UIManager.LookAndFeelInfo info : javax.swing.UIManager.getInstalledLookAndFeels()) {
                if ("Nimbus".equals(info.getName())) {
                    javax.swing.UIManager.setLookAndFeel(info.getClassName());
                    break;
                }
            }
        } catch (ClassNotFoundException ex) {
            java.util.logging.Logger.getLogger(MainForm.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (InstantiationException ex) {
            java.util.logging.Logger.getLogger(MainForm.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (IllegalAccessException ex) {
            java.util.logging.Logger.getLogger(MainForm.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (javax.swing.UnsupportedLookAndFeelException ex) {
            java.util.logging.Logger.getLogger(MainForm.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        }
        //</editor-fold>

        /* Create and display the form */
        
        java.awt.EventQueue.invokeLater(new Runnable() {
            public void run() {
                new MainForm().setVisible(true);
            }
        });
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.ButtonGroup buttonGroup1;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel9;
    private javax.swing.JLabel jLabelReporte001;
    private javax.swing.JLabel jLabelReporte002;
    private javax.swing.JLabel jLabelReporte003;
    private javax.swing.JLabel jLabelReporte004;
    private javax.swing.JLabel jLabelReporte005;
    private javax.swing.JRadioButton jRadioButtonDirectionalDrillers;
    private javax.swing.JRadioButton jRadioButtonDrillinglEngineers;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JTextPane jTextPane1;
    // End of variables declaration//GEN-END:variables
}
