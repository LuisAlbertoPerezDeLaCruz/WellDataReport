/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package my;

import java.awt.Cursor;
import java.awt.TrayIcon;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.DecimalFormat;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JOptionPane;
import javax.swing.Timer;
import miLibreria.Numeros;
import static miLibreria.GlobalConstants.valorNulo;
import static miLibreria.GlobalConstants.valorNuloMuestra;
import miLibreria.ManejoDeCombos;
import miLibreria.bd.*;


/**
 *
 * @author Luis
 */
public class Rpt002 extends javax.swing.JDialog {
    public ManejoBDI oBD;
    private ManejoDeCombos oManejoDeCombos; 
    String sDir;
    public double tvdDesde, tvdHasta;
    public long clienteId=0,campoId=0, macollaId=0,sectionSubTypeId=0;
    public Numeros oNumeros=new Numeros();
    public Object[][] aCorridas;
    public int cantCorridas;
    public Double aSurvey[];
    private Calculos oCalc=null;
    
    public Rpt002(java.awt.Frame parent, boolean modal,ManejoBDI o ) {
        super(parent, modal);
        initComponents();
        oBD=o;
        oManejoDeCombos = new ManejoDeCombos();        
        seteosIniciales();
        oManejoDeCombos.llenaCombo(oBD,oManejoDeCombos.getModeloCombo(),DrillingSubSectionType.class,this.jComboBoxDrillingSubType,"Selection Sub-Section Type");
        this.jTablePozos.getColumnModel().getColumn(3).setPreferredWidth(150);
        this.jTablePozos.getColumnModel().getColumn(5).setPreferredWidth(150);
        this.jTablePozos.getColumnModel().getColumn(6).setPreferredWidth(100);
        this.jTablePozos.getColumnModel().getColumn(7).setPreferredWidth(120);
    }
    
    
    public void seteosIniciales() {
        int tiempoEnMilisegundos = 500;
        Timer timer = new Timer (tiempoEnMilisegundos, new ActionListener () { 
        public void actionPerformed(ActionEvent e) { 
            boolean ok=true;
            tvdDesde=0.0;
            tvdHasta=0.0;
             
            if (jTextFieldTvdDesde.getText().length()>0) {
                if (jTextFieldTvdDesde.getText().matches("-?\\d+(\\.\\d+)?")) {
                 tvdDesde=oNumeros.valorDouble(jTextFieldTvdDesde.getText());                 
                }
            }
            if (jTextFieldTvdHasta.getText().length()>0) {
                if (jTextFieldTvdHasta.getText().matches("-?\\d+(\\.\\d+)?")) {
                 tvdHasta=oNumeros.valorDouble(jTextFieldTvdHasta.getText());                 
                }
            }
             
             if ((tvdDesde>0.0 && tvdHasta>0.0) && (tvdDesde<=tvdHasta) ) {
             } else {
                 ok=false; 
             } 
             if (clienteId+campoId+macollaId==0) ok=false;
             
             if (Rpt002.this.jRadioButtonCliente.isSelected())  {
                 if (clienteId==0) ok=false;
             }
             if (Rpt002.this.jRadioButtonCampo.isSelected())  {
                 if (clienteId==0) ok=false;
                 if (campoId==0) ok=false;
             }
             if (Rpt002.this.jRadioButtonMacolla.isSelected())  {
                 if (clienteId==0) ok=false;
                 if (campoId==0) ok=false;
                 if (macollaId==0) ok=false;                 
             }
             if (sectionSubTypeId==0) ok=false;
             
             jButtonBuscarPozos.setEnabled(ok);
             pack();
        } 
        });
        timer.start();
    }
    
private void buscarPozos() {
        String s;
        ResultSet rs;
        int i=0;
        int cantPozos=0;
        limpiarTablaPozos();
        Object[][] aPozos = null;
        String prevPozo=null, currPozo=null;
        Calculos oCalc=new Calculos(oBD);
        long wellId=0;
        
        final int POZOS_CAMPO=0;
        final int POZOS_MACOLLA=1;
        final int POZOS_POZO=2;
        final int POZOS_DLS=3;
        final int POZOS_SLIDING_STEERING=4;
        final int POZOS_CONTADOR=5;
        final int POZOS_TECHNOLOGY=6;
        final int POZOS_SIZE=7;
        final int POZOS_PORCENTAJE_ARENA=8;
        final int POZOS_CONTADOR_2=9;
        
        final int CORRIDAS_RUN_ID=0;        
        final int CORRIDAS_MD=1;       
        final int CORRIDAS_DLS=2;       
        final int CORRIDAS_SLIDING_STEERING=3;       
        final int CORRIDAS_CAMPO=4;
        final int CORRIDAS_MACOLLA=5;        
        final int CORRIDAS_POZO=6;
        final int CORRIDAS_PORCENTAJE_ARENA=7;

//        s="SELECT DISTINCT clientesNombre, campoNombre, macollaNombre, wellId, wellNombre, sectionsNumeroIdentificador, runNumero, runId, md, dls ";
//        s+="FROM ConsultaCorridasPorCriteria1 WHERE tvd >=" + this.tvdDesde + " AND tvd<=" + this.tvdHasta ;
//        s+=" AND clientesId="+clienteId + " AND drillingSubSectionId=" + this.sectionSubTypeId;
        
        cursorEspera();

        s="SELECT DISTINCT ConsultaCorridasPorCriteria1.clientesNombre, ConsultaCorridasPorCriteria1.campoNombre, ConsultaCorridasPorCriteria1.macollaNombre, ConsultaCorridasPorCriteria1.wellId, ConsultaCorridasPorCriteria1.wellNombre, ConsultaCorridasPorCriteria1.sectionsNumeroIdentificador, ConsultaCorridasPorCriteria1.runNumero, ConsultaCorridasPorCriteria1.runId, ConsultaCorridasPorCriteria1.md, ConsultaCorridasPorCriteria1.dls, BHA.tipoDT, Diameters.size\n" +
          "FROM Diameters INNER JOIN (Sections INNER JOIN (BHA INNER JOIN ConsultaCorridasPorCriteria1 ON BHA.runId = ConsultaCorridasPorCriteria1.runID) ON Sections.id = ConsultaCorridasPorCriteria1.sectionsId) ON Diameters.id = Sections.diameterId ";
        s+="WHERE tvd >=" + this.tvdDesde + " AND tvd<=" + this.tvdHasta +" AND clientesId="+clienteId + " AND drillingSubSectionId=" + this.sectionSubTypeId+" ";
        
        if (campoId>0) {
            s+=" AND campoId="+campoId;    
        }
        if (macollaId>0) {
            s+=" AND macollaId="+macollaId;    
        } 
        s+=" ORDER BY wellNombre, runId ASC;";
        
        rs=oBD.select(s);
        if (rs==null) return;
        
        try {            
            while (rs.next()) {                
                i++;
            }
            cantCorridas=i;
            aCorridas = new Object[i][8];
            aPozos=new Object[i][10];
            i=0;
            
            rs.beforeFirst();
            while (rs.next()) {
                if (prevPozo==null && currPozo==null) {
                   currPozo=rs.getString("wellNombre");
                   aPozos[cantPozos][POZOS_CAMPO]=rs.getString("campoNombre");
                   aPozos[cantPozos][POZOS_MACOLLA]=rs.getString("macollaNombre");
                   aPozos[cantPozos][POZOS_TECHNOLOGY]=rs.getString("tipoDT");
                   aPozos[cantPozos][POZOS_SIZE]=rs.getDouble("size");
                   aPozos[cantPozos][POZOS_POZO]=currPozo;
                   prevPozo=currPozo;
                   cantPozos++;
                   wellId=rs.getLong("wellId");
                   oCalc.cargarPozo(rs.getLong("wellId"));
                }
                currPozo=rs.getString("wellNombre");
                wellId=rs.getLong("wellId");
                if (!currPozo.equals(prevPozo)) {
                   oCalc.cargarPozo(rs.getLong("wellId"));
                   aPozos[cantPozos][POZOS_CAMPO]=rs.getString("campoNombre");
                   aPozos[cantPozos][POZOS_MACOLLA]=rs.getString("macollaNombre");
                   aPozos[cantPozos][POZOS_POZO]=currPozo;
                   aPozos[cantPozos][POZOS_TECHNOLOGY]=rs.getString("tipoDT");
                   aPozos[cantPozos][POZOS_SIZE]=rs.getDouble("size");
                   prevPozo=currPozo;
                   cantPozos++;
                }
                double md=0.0,dls=0.0;
                md=rs.getDouble("md");
                dls=rs.getDouble("dls");
                
                aCorridas[i][CORRIDAS_RUN_ID]=rs.getLong("runId");
                aCorridas[i][CORRIDAS_MD]=md;
                aCorridas[i][CORRIDAS_DLS]=dls;
                
                double porcentajeSliding=0.0, porcentajeSteering=0.0;
                double porcentajeArena=0.0;

                porcentajeSliding=oCalc.getPorcentajeSliding(md);
                porcentajeSteering=oCalc.getPorcentajeSteering(md);
                porcentajeArena=oCalc.getPorcentajeArena(md);
                
                aCorridas[i][CORRIDAS_SLIDING_STEERING]=0.0;
                aCorridas[i][CORRIDAS_PORCENTAJE_ARENA]=0.0;
                
                if (porcentajeSliding>0)
                    aCorridas[i][CORRIDAS_SLIDING_STEERING]=porcentajeSliding;
                if (porcentajeSteering>0)
                    aCorridas[i][CORRIDAS_SLIDING_STEERING]=porcentajeSteering;
                if (porcentajeArena>0)
                    aCorridas[i][CORRIDAS_PORCENTAJE_ARENA]=porcentajeArena; 
                        
                aCorridas[i][CORRIDAS_CAMPO]=rs.getString("campoNombre");
                aCorridas[i][CORRIDAS_MACOLLA]=rs.getString("macollaNombre");
                aCorridas[i][CORRIDAS_POZO]=rs.getString("wellNombre");
                               
                i++;
            }
        } catch (SQLException ex) {
            Logger.getLogger(Rpt002.class.getName()).log(Level.SEVERE, null, ex);
        }

        for (int p=0;p<=cantCorridas-1;p++) {
            for (int j=0;j<=aPozos.length-1;j++) {
                if (aCorridas[p][CORRIDAS_POZO].equals(aPozos[j][POZOS_POZO])) {
                    if (aPozos[j][POZOS_DLS]==null) aPozos[j][POZOS_DLS]= (Double) 0.0;
                    if (aPozos[j][POZOS_SLIDING_STEERING]==null) aPozos[j][POZOS_SLIDING_STEERING]= (Double) 0.0;
                    if (aPozos[j][POZOS_CONTADOR]==null) aPozos[j][POZOS_CONTADOR]= (Double) 0.0;
                    if (aPozos[j][POZOS_CONTADOR_2]==null) aPozos[j][POZOS_CONTADOR_2]= (Double) 0.0;
                    if (aPozos[j][POZOS_PORCENTAJE_ARENA]==null) aPozos[j][POZOS_PORCENTAJE_ARENA]= (Double) 0.0;
                    if ((Double) aCorridas[p][CORRIDAS_SLIDING_STEERING]==0) continue;
                    aPozos[j][POZOS_DLS] =(Double) aPozos[j][POZOS_DLS] + (Double) aCorridas[p][CORRIDAS_DLS];
                    aPozos[j][POZOS_SLIDING_STEERING] =(Double) aPozos[j][POZOS_SLIDING_STEERING] + (Double) aCorridas[p][CORRIDAS_SLIDING_STEERING];
                    aPozos[j][POZOS_CONTADOR] =(Double) aPozos[j][POZOS_CONTADOR] + 1;
                    if ((Double) aCorridas[p][CORRIDAS_PORCENTAJE_ARENA]==0) continue;
                    aPozos[j][POZOS_PORCENTAJE_ARENA]=(Double) aPozos[j][POZOS_PORCENTAJE_ARENA] + (Double) aCorridas[p][CORRIDAS_PORCENTAJE_ARENA];
                    aPozos[j][POZOS_CONTADOR_2] =(Double) aPozos[j][POZOS_CONTADOR_2] + 1;
                }                
            }
        }
        int y=0;
        for (int p=0;p<=cantPozos-1;p++) {
            if ((Double) aPozos[p][POZOS_SLIDING_STEERING]==0) continue;
            aPozos[p][POZOS_DLS]=(Double) aPozos[p][POZOS_DLS]/(Double) aPozos[p][POZOS_CONTADOR];
            aPozos[p][POZOS_SLIDING_STEERING]=(Double) aPozos[p][POZOS_SLIDING_STEERING]/(Double) aPozos[p][POZOS_CONTADOR]; 
            aPozos[p][POZOS_PORCENTAJE_ARENA]=(Double) aPozos[p][POZOS_PORCENTAJE_ARENA]/(Double) aPozos[p][POZOS_CONTADOR_2]; 
            this.jTablePozos.setValueAt(aPozos[p][POZOS_CAMPO], y, 0);
            this.jTablePozos.setValueAt(aPozos[p][POZOS_MACOLLA], y, 1);
            this.jTablePozos.setValueAt(aPozos[p][POZOS_POZO], y, 2);
            this.jTablePozos.setValueAt(na(aPozos[p][POZOS_DLS]), y, 3);
            this.jTablePozos.setValueAt(na(aPozos[p][POZOS_SLIDING_STEERING]), y, 4);
            this.jTablePozos.setValueAt(na((double) aPozos[p][POZOS_DLS] * 100 /(double)aPozos[p][POZOS_SLIDING_STEERING]), y, 5);
            this.jTablePozos.setValueAt(aPozos[p][POZOS_TECHNOLOGY],y,6);
            this.jTablePozos.setValueAt(aPozos[p][POZOS_SIZE],y,7);
            this.jTablePozos.setValueAt(na(aPozos[p][POZOS_PORCENTAJE_ARENA]), y, 8);
            y++;
        }
        
        cursorNormal();
    } 

    private void cargarSurvey(Long runId, Double md) {
       Survey oSurvey=new Survey();
       SurveyPerMD oSurveyPerMd=new SurveyPerMD();
       Object[] o;
       long surveyId=0;
       final int actual=1,anterior=0;
       aSurvey=new Double[2];
       try {
            oSurvey=(Survey) oBD.select(Survey.class, "runId="+runId)[0];
            surveyId=oSurvey.getId();
            o=oBD.select(SurveyPerMD.class, "surveyId="+surveyId);
            for (int i=0;i<=o.length-1;i++) {
                oSurveyPerMd=(SurveyPerMD) o[i];
                if (oSurveyPerMd.getMd()==md) {
                    aSurvey[actual]=oSurveyPerMd.getMd();
                    if ((i-1)>=0) oSurveyPerMd=(SurveyPerMD) o[i-1];
                    aSurvey[anterior]=oSurveyPerMd.getMd();
                }
            }
        } catch (InstantiationException | IllegalAccessException ex) {
            Logger.getLogger(Rpt001.class.getName()).log(Level.SEVERE, null, ex);
        }        
    }
    
    private void limpiarTablaPozos() {
        for (int i=0;i<=this.jTablePozos.getRowCount()-1;i++) {
            for (int j=0;j<=this.jTablePozos.getColumnCount()-1;j++) {
                this.jTablePozos.setValueAt("", i, j);
            }
        }
    }
    
    
    private void prepararControlesDeCriteria() {
        if (this.jRadioButtonCliente.isSelected()) {
            this.jComboBoxClientes.setEnabled(true);
            this.jComboBoxCampo.setEnabled(false);
            this.jComboBoxMacolla.setEnabled(false);
        }
        if (this.jRadioButtonCampo.isSelected()) {
            this.jComboBoxClientes.setEnabled(true);
            this.jComboBoxCampo.setEnabled(true);
            this.jComboBoxMacolla.setEnabled(false);            
        }
        if (this.jRadioButtonMacolla.isSelected()) {
            this.jComboBoxClientes.setEnabled(true);
            this.jComboBoxCampo.setEnabled(true);
            this.jComboBoxMacolla.setEnabled(true);            
        }
        oManejoDeCombos.llenaCombo(oBD,oManejoDeCombos.getModeloCombo(),Clientes.class,this.jComboBoxClientes,"Seleccione Cliente"); 
        limpiarTablaPozos();
    }
    
    public Object na(Object o) {
        DecimalFormat df = new DecimalFormat("##########0.000");
        if (o.toString().contains(""+valorNulo)) {
            return valorNuloMuestra;
        } else {
            try {
            return df.format(o).replace(',', '.');
            } catch (IllegalArgumentException ex) {
                return o;
            }
        }
    }
    
    public void msgbox(String s, String t){
        JOptionPane.showMessageDialog(null, s,t,TrayIcon.MessageType.WARNING.ordinal());        
    }

    public void cursorNormal(){
        this.setCursor(Cursor.getDefaultCursor());
    }
    
    public void cursorEspera(){
        this.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
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
        jPanel1 = new javax.swing.JPanel();
        jLabel1 = new javax.swing.JLabel();
        jRadioButtonCliente = new javax.swing.JRadioButton();
        jRadioButtonCampo = new javax.swing.JRadioButton();
        jRadioButtonMacolla = new javax.swing.JRadioButton();
        jComboBoxClientes = new javax.swing.JComboBox();
        jComboBoxCampo = new javax.swing.JComboBox();
        jComboBoxMacolla = new javax.swing.JComboBox();
        jTextFieldTvdDesde = new javax.swing.JTextField();
        jTextFieldTvdHasta = new javax.swing.JTextField();
        jLabel2 = new javax.swing.JLabel();
        jLabel3 = new javax.swing.JLabel();
        jButtonBuscarPozos = new javax.swing.JButton();
        jComboBoxDrillingSubType = new javax.swing.JComboBox();
        jLabel8 = new javax.swing.JLabel();
        jScrollPanePozos = new javax.swing.JScrollPane();
        jTablePozos = new javax.swing.JTable();
        jLabel9 = new javax.swing.JLabel();

        setTitle("Well Planning Offset Data");
        setMinimumSize(new java.awt.Dimension(675, 500));
        setModal(true);
        getContentPane().setLayout(new org.netbeans.lib.awtextra.AbsoluteLayout());

        jPanel1.setBorder(javax.swing.BorderFactory.createEtchedBorder());
        jPanel1.setLayout(new org.netbeans.lib.awtextra.AbsoluteLayout());

        jLabel1.setText("Selection criteria:");
        jPanel1.add(jLabel1, new org.netbeans.lib.awtextra.AbsoluteConstraints(10, 0, -1, -1));

        buttonGroup1.add(jRadioButtonCliente);
        jRadioButtonCliente.setText("Client");
        jRadioButtonCliente.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jRadioButtonClienteActionPerformed(evt);
            }
        });
        jPanel1.add(jRadioButtonCliente, new org.netbeans.lib.awtextra.AbsoluteConstraints(20, 20, -1, -1));

        buttonGroup1.add(jRadioButtonCampo);
        jRadioButtonCampo.setText("Field");
        jRadioButtonCampo.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jRadioButtonCampoActionPerformed(evt);
            }
        });
        jPanel1.add(jRadioButtonCampo, new org.netbeans.lib.awtextra.AbsoluteConstraints(20, 40, -1, -1));

        buttonGroup1.add(jRadioButtonMacolla);
        jRadioButtonMacolla.setText("Pad");
        jRadioButtonMacolla.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jRadioButtonMacollaActionPerformed(evt);
            }
        });
        jPanel1.add(jRadioButtonMacolla, new org.netbeans.lib.awtextra.AbsoluteConstraints(20, 60, -1, -1));

        jComboBoxClientes.setEnabled(false);
        jComboBoxClientes.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jComboBoxClientesActionPerformed(evt);
            }
        });
        jPanel1.add(jComboBoxClientes, new org.netbeans.lib.awtextra.AbsoluteConstraints(160, 10, 230, -1));

        jComboBoxCampo.setEnabled(false);
        jComboBoxCampo.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jComboBoxCampoActionPerformed(evt);
            }
        });
        jPanel1.add(jComboBoxCampo, new org.netbeans.lib.awtextra.AbsoluteConstraints(160, 40, 230, -1));

        jComboBoxMacolla.setEnabled(false);
        jComboBoxMacolla.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jComboBoxMacollaActionPerformed(evt);
            }
        });
        jPanel1.add(jComboBoxMacolla, new org.netbeans.lib.awtextra.AbsoluteConstraints(160, 70, 230, -1));

        jTextFieldTvdDesde.setHorizontalAlignment(javax.swing.JTextField.RIGHT);
        jTextFieldTvdDesde.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jTextFieldTvdDesdeActionPerformed(evt);
            }
        });
        jTextFieldTvdDesde.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyTyped(java.awt.event.KeyEvent evt) {
                jTextFieldTvdDesdeKeyTyped(evt);
            }
        });
        jPanel1.add(jTextFieldTvdDesde, new org.netbeans.lib.awtextra.AbsoluteConstraints(160, 100, 70, -1));

        jTextFieldTvdHasta.setHorizontalAlignment(javax.swing.JTextField.RIGHT);
        jTextFieldTvdHasta.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jTextFieldTvdHastaActionPerformed(evt);
            }
        });
        jTextFieldTvdHasta.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyTyped(java.awt.event.KeyEvent evt) {
                jTextFieldTvdHastaKeyTyped(evt);
            }
        });
        jPanel1.add(jTextFieldTvdHasta, new org.netbeans.lib.awtextra.AbsoluteConstraints(320, 100, 70, -1));

        jLabel2.setText("TVD to (ft):");
        jPanel1.add(jLabel2, new org.netbeans.lib.awtextra.AbsoluteConstraints(250, 100, -1, -1));

        jLabel3.setText("TVD from (ft):");
        jPanel1.add(jLabel3, new org.netbeans.lib.awtextra.AbsoluteConstraints(70, 100, -1, -1));

        jButtonBuscarPozos.setText("Search wells");
        jButtonBuscarPozos.setEnabled(false);
        jButtonBuscarPozos.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButtonBuscarPozosActionPerformed(evt);
            }
        });
        jPanel1.add(jButtonBuscarPozos, new org.netbeans.lib.awtextra.AbsoluteConstraints(510, 100, -1, -1));

        jComboBoxDrillingSubType.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jComboBoxDrillingSubTypeActionPerformed(evt);
            }
        });
        jPanel1.add(jComboBoxDrillingSubType, new org.netbeans.lib.awtextra.AbsoluteConstraints(410, 40, 210, -1));

        getContentPane().add(jPanel1, new org.netbeans.lib.awtextra.AbsoluteConstraints(20, 10, 630, 130));

        jLabel8.setText("Pozos:");
        getContentPane().add(jLabel8, new org.netbeans.lib.awtextra.AbsoluteConstraints(20, 150, -1, -1));

        jTablePozos.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {
                {null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null}
            },
            new String [] {
                "Field", "Pad", "Well", "Average DLS (°/100ft)", "% Steering", "Yield extrapolated 100%", "Technology ", "Hole Size (in) ", "Sand %"
            }
        ) {
            boolean[] canEdit = new boolean [] {
                false, false, false, false, false, false, false, false, false
            };

            public boolean isCellEditable(int rowIndex, int columnIndex) {
                return canEdit [columnIndex];
            }
        });
        jTablePozos.setAutoResizeMode(javax.swing.JTable.AUTO_RESIZE_OFF);
        jTablePozos.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                jTablePozosMouseClicked(evt);
            }
        });
        jScrollPanePozos.setViewportView(jTablePozos);

        getContentPane().add(jScrollPanePozos, new org.netbeans.lib.awtextra.AbsoluteConstraints(20, 170, 630, 220));

        jLabel9.setIcon(new javax.swing.ImageIcon(getClass().getResource("/my/SB.png"))); // NOI18N
        jLabel9.setBorder(javax.swing.BorderFactory.createEtchedBorder());
        jLabel9.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                jLabel9MouseClicked(evt);
            }
        });
        getContentPane().add(jLabel9, new org.netbeans.lib.awtextra.AbsoluteConstraints(20, 420, 140, 40));

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void jRadioButtonClienteActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jRadioButtonClienteActionPerformed
        prepararControlesDeCriteria();
    }//GEN-LAST:event_jRadioButtonClienteActionPerformed

    private void jRadioButtonCampoActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jRadioButtonCampoActionPerformed
        prepararControlesDeCriteria();
    }//GEN-LAST:event_jRadioButtonCampoActionPerformed

    private void jRadioButtonMacollaActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jRadioButtonMacollaActionPerformed
        prepararControlesDeCriteria();
    }//GEN-LAST:event_jRadioButtonMacollaActionPerformed

    private void jComboBoxClientesActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jComboBoxClientesActionPerformed
        clienteId=oManejoDeCombos.getComboID(this.jComboBoxClientes);
        String s="SELECT campoId,campoNombre from ConsultaCampoCliente1 WHERE clienteId="+clienteId;
        oManejoDeCombos.llenaCombo(oBD,oManejoDeCombos.getModeloCombo(),s,this.jComboBoxCampo,"Seleccione Campo");
    }//GEN-LAST:event_jComboBoxClientesActionPerformed

    private void jComboBoxCampoActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jComboBoxCampoActionPerformed
        clienteId=oManejoDeCombos.getComboID(this.jComboBoxClientes);
        campoId=oManejoDeCombos.getComboID(this.jComboBoxCampo);
        String s="SELECT macollaId,macollaNombre from ConsultaMacolla1 WHERE clienteId="+clienteId + " AND campoId="+campoId;
        oManejoDeCombos.llenaCombo(oBD,oManejoDeCombos.getModeloCombo(),s,this.jComboBoxMacolla,"Seleccione Macolla");
    }//GEN-LAST:event_jComboBoxCampoActionPerformed

    private void jComboBoxMacollaActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jComboBoxMacollaActionPerformed
        macollaId=oManejoDeCombos.getComboID(this.jComboBoxMacolla);
    }//GEN-LAST:event_jComboBoxMacollaActionPerformed

    private void jTextFieldTvdDesdeActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jTextFieldTvdDesdeActionPerformed

    }//GEN-LAST:event_jTextFieldTvdDesdeActionPerformed

    private void jTextFieldTvdDesdeKeyTyped(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_jTextFieldTvdDesdeKeyTyped
        oNumeros.soloDobles(evt);
    }//GEN-LAST:event_jTextFieldTvdDesdeKeyTyped

    private void jTextFieldTvdHastaActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jTextFieldTvdHastaActionPerformed

    }//GEN-LAST:event_jTextFieldTvdHastaActionPerformed

    private void jTextFieldTvdHastaKeyTyped(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_jTextFieldTvdHastaKeyTyped
        oNumeros.soloDobles(evt);
    }//GEN-LAST:event_jTextFieldTvdHastaKeyTyped

    private void jButtonBuscarPozosActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButtonBuscarPozosActionPerformed
        buscarPozos();
    }//GEN-LAST:event_jButtonBuscarPozosActionPerformed

    private void jTablePozosMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_jTablePozosMouseClicked

    }//GEN-LAST:event_jTablePozosMouseClicked

    private void jLabel9MouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_jLabel9MouseClicked
        msgbox(MainForm.msgVersion,"About WellDataReport");
    }//GEN-LAST:event_jLabel9MouseClicked

    private void jComboBoxDrillingSubTypeActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jComboBoxDrillingSubTypeActionPerformed
        sectionSubTypeId=oManejoDeCombos.getComboID(this.jComboBoxDrillingSubType);
    }//GEN-LAST:event_jComboBoxDrillingSubTypeActionPerformed

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
            java.util.logging.Logger.getLogger(Rpt002.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (InstantiationException ex) {
            java.util.logging.Logger.getLogger(Rpt002.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (IllegalAccessException ex) {
            java.util.logging.Logger.getLogger(Rpt002.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (javax.swing.UnsupportedLookAndFeelException ex) {
            java.util.logging.Logger.getLogger(Rpt002.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        }
        //</editor-fold>
        //</editor-fold>
        //</editor-fold>
        //</editor-fold>

        /* Create and display the dialog */
        java.awt.EventQueue.invokeLater(new Runnable() {
            public void run() {
                Rpt002 dialog = new Rpt002(new javax.swing.JFrame(), true,new ManejoBDAccess());
                dialog.addWindowListener(new java.awt.event.WindowAdapter() {
                    @Override
                    public void windowClosing(java.awt.event.WindowEvent e) {
                        System.exit(0);
                    }
                });
                dialog.setVisible(true);
            }
        });
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.ButtonGroup buttonGroup1;
    public javax.swing.JButton jButtonBuscarPozos;
    private javax.swing.JComboBox jComboBoxCampo;
    private javax.swing.JComboBox jComboBoxClientes;
    private javax.swing.JComboBox jComboBoxDrillingSubType;
    private javax.swing.JComboBox jComboBoxMacolla;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel8;
    private javax.swing.JLabel jLabel9;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JRadioButton jRadioButtonCampo;
    private javax.swing.JRadioButton jRadioButtonCliente;
    private javax.swing.JRadioButton jRadioButtonMacolla;
    private javax.swing.JScrollPane jScrollPanePozos;
    private javax.swing.JTable jTablePozos;
    private javax.swing.JTextField jTextFieldTvdDesde;
    private javax.swing.JTextField jTextFieldTvdHasta;
    // End of variables declaration//GEN-END:variables
}
