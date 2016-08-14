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
import java.io.File;
import java.io.FileOutputStream;
import static java.lang.Double.parseDouble;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import javax.swing.Timer;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.swing.table.DefaultTableModel;
import miLibreria.ManejoDeCombos;
import static miLibreria.ManejoDeCombos.modeloCombo;
import miLibreria.bd.Clientes;
import miLibreria.bd.DrillingSubSectionType;
import miLibreria.bd.ManejoBDAccess;
import miLibreria.bd.ManejoBDI;
import org.apache.poi.hssf.usermodel.HSSFRow;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;



/**
 *
 * @author USUARIO
 */
public class Rpt005 extends javax.swing.JDialog {

    public long clienteId=0,campoId=0, macollaId=0;
    public ManejoBDI oBD;
    private final ManejoDeCombos oManejoDeCombos; 
    private final javax.swing.JComboBox jComboBoxSubSections = new javax.swing.JComboBox();
    private final DecimalFormat df = new DecimalFormat("#0.000");
    private DefaultTableModel modelTable1;
    private boolean procesado=false;
    
    public Rpt005(java.awt.Frame parent, boolean modal,ManejoBDI o ) {
        super(parent, modal);
        initComponents();
        setearTimer();

        oBD=o;
        oManejoDeCombos = new ManejoDeCombos(); 
        oManejoDeCombos.llenaCombo(oBD,oManejoDeCombos.getModeloCombo(),Clientes.class,this.jComboBoxClientes,"Seleccione Cliente"); 

        oManejoDeCombos.llenaCombo(oBD,modeloCombo,DrillingSubSectionType.class,this.jComboBoxSubSections,"");
        jComboBoxSubSections.setEnabled(true);
        jComboBoxSubSections.setVisible(true);
        
    }
    
    public final void setearTimer() {
        int tiempoEnMilisegundos = 1000;
        Timer timer = new Timer (tiempoEnMilisegundos, new ActionListener () { 
        public void actionPerformed(ActionEvent e) { 
            if (jButtonExportar.isEnabled() && !procesado) {
                modelTable1 = new DefaultTableModel();
                jTable1.setModel(modelTable1);                
            }
            boolean ok=true;
            if (campoId<=0 || clienteId<=0 || macollaId<=0) ok=false;
            jButtonProcesar.setEnabled(ok);
            jButtonExportar.setEnabled(false);
            if (procesado) jButtonExportar.setEnabled(true);
            pack();
        } 
        });
        timer.start();
    }
    
    private void procesar(){
        cursorEspera();
        procesarTabla1();
        cursorNormal();
    }
          
    private void procesarTabla1() {
        String s="";
        ResultSet rs;
        Calculos oCalc;
        oCalc=new Calculos(oBD);
        double maxTVD=0.0;
        double tvd=0.0,md=0.0, porcentajeSliding=0.0, dls=0.0;
        String wellNombre="";
        String description="";
        List<String> listOfColumnas = new ArrayList<>();
        SumatoriaDLS[][] resultado=null;
        PromedioDLS[] promedio=null;
        int rows=0, row=0,col=0;
        long prevWellId=0, currWellId=0;
        boolean primerRegistro=true;
        
        modelTable1 = new DefaultTableModel();
        jTable1.setModel(modelTable1);
        
        //Segmento tabla1
       
        s="SELECT CampoCliente.campoId, Well.macollaId, Well.id AS wellId, Well.nombre as wellNombre, Run.Id, Survey.id, SurveyPerMD.md, SurveyPerMD.tvd, SurveyPerMD.dls, BHA.tipoDT, DrillingSubSectionType.description\n" +
        "FROM (BHA INNER JOIN (RunSubSection INNER JOIN ((Macolla INNER JOIN (SurveyPerMD INNER JOIN (Survey INNER JOIN (Run INNER JOIN (Sections INNER JOIN Well ON Sections.wellId = Well.id) ON Run.sectionId = Sections.id) ON Survey.runId = Run.Id) ON SurveyPerMD.surveyId = Survey.id) ON Macolla.id = Well.macollaId) INNER JOIN CampoCliente ON Macolla.campoClienteId = CampoCliente.id) ON RunSubSection.runID = Run.Id) ON BHA.runId = Run.Id) INNER JOIN DrillingSubSectionType ON RunSubSection.drillingSubSectionId = DrillingSubSectionType.id\n" +
        "WHERE (((CampoCliente.campoId)="+campoId+") AND ((Well.macollaId)="+macollaId+") AND ((SurveyPerMD.md)>=[profundidadInicial] And (SurveyPerMD.md)<=[profundidadFinal]))\n" +
        "ORDER BY Well.macollaId, Well.id, SurveyPerMD.tvd;";
        
        rs=oBD.select(s);
        try {
            while (rs.next()){
                if (listOfColumnas.contains(rs.getString("wellNombre"))==false) {
                    listOfColumnas.add(rs.getString("wellNombre"));
                }
                if (rs.getDouble("tvd")>maxTVD) maxTVD=rs.getDouble("tvd");
            }
            //Dimensiono el array de resultados primero que todo
            rows=(int) Math.round(maxTVD/50);
            rows += (maxTVD%50)>0 ? 1:0 ;
            resultado=new SumatoriaDLS[rows][listOfColumnas.size()];
            promedio=new PromedioDLS[rows];
            row=0;
            for (int i=0;i<= resultado.length-1;i++){
                promedio[i]=new PromedioDLS(row,row+50);
                for (int j=0;j<=resultado[i].length-1;j++){
                    resultado[i][j]=new SumatoriaDLS(row,row+50,listOfColumnas.get(j));
                }
                row+=50;
            }
            
            //Ahora leo los datos para sumarizarlos 
            rs.beforeFirst();
            while (rs.next()){
                if (primerRegistro) {
                    primerRegistro=false;
                    currWellId=rs.getLong("wellId");
                    prevWellId=currWellId;
                    oCalc.cargarPozo(currWellId);
                } else {
                  prevWellId=currWellId;
                  currWellId=rs.getLong("wellId");  
                }
                if (prevWellId!=currWellId) {
                   oCalc.cargarPozo(currWellId); 
                }
                tvd=rs.getDouble("tvd");
                if (tvd<0) continue;
                md=rs.getDouble("md");
                dls=rs.getDouble("dls");
                description=rs.getString("description");
                wellNombre=rs.getString("wellNombre");
                if ("MOTOR".equals(rs.getString("tipoDT"))){
                    porcentajeSliding=oCalc.getPorcentajeSliding(md);
                } else {
                    porcentajeSliding=oCalc.getPorcentajeSteering(md);
                }
                //ubico la fila y la columna en el array de resultados
                row=(int) Math.floor(tvd/50);
                for (int r=0;r<=resultado.length-1;r++){
                    if (tvd>=resultado[r][0].getTvdDesde() && tvd<=resultado[r][0].getTvdHasta()){
                        row=r;
                        break;
                    }
                }
                col=listOfColumnas.indexOf(wellNombre);
                resultado[row][col].sumaDLS(dls);
                resultado[row][col].sumaPorcentajeSliding(porcentajeSliding);
                promedio[row].suma(dls,porcentajeSliding, description);
            }            
        } catch (SQLException ex) {
            Logger.getLogger(Rpt005.class.getName()).log(Level.SEVERE, null, ex);
        }
        //Ya con los resultados parciales calculo el dls al 100% y lo plasmo en pantalla
        //Recorro primero por columnas (Pozos) y las agrego al modelo
        modelTable1.addColumn("TVD");
        for (String pozo : listOfColumnas) {
            modelTable1.addColumn(pozo);
        }
        modelTable1.addColumn("Avg(no turn)");
        modelTable1.addColumn("Avg(Turn right)");
        modelTable1.addColumn("Avg(Turn left)");

        Object[] fila=new Object[resultado[0].length+4];
        int offset=0;
        
        for (row=0;row<=resultado.length-1;row++) {
            offset=0;
            fila=new Object[resultado[row].length+4];
            fila[offset]=resultado[row][0].getTvdHasta();
            offset=1;
            for (col=0;col<=resultado[0].length-1;col++){
                if (resultado[row][col].getDls100()>0)
                   fila[offset+col]=resultado[row][col].getDls100();
            }
            offset+=col;
            if (promedio[row].getDls100NoTurn()>0)
                fila[offset]=promedio[row].getDls100NoTurn();
            if (promedio[row].getDls100TurnRight()>0)
                fila[offset+1]=promedio[row].getDls100TurnRight();
            if (promedio[row].getDls100TurnLeft()>0)
                fila[offset+2]=promedio[row].getDls100TurnLeft();
            modelTable1.addRow(fila);
        }
        procesado=true;
    }
        
    public void exportar(int tab) {
        HSSFSheet sheet=null;
        HSSFRow[] rowhead=null ;
        HSSFWorkbook workbook = null; 
        int rows=0;
        int row=0;
        int cols=0;
        String archivo="";
        File currDirectory;
        currDirectory=new File(System.getProperty("user.home"));
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setCurrentDirectory(currDirectory);
        fileChooser.setDialogTitle("Escoja el directorio/carpeta");
        FileNameExtensionFilter filter = new FileNameExtensionFilter("Archivos Excel", new String[] {"xls", "xlsx"});
        fileChooser.setFileFilter(filter);
        int result= fileChooser.showSaveDialog(this);
        File fn=fileChooser.getSelectedFile();
        try {
            archivo=fn.getPath()+".xls";
        } catch (NullPointerException ex) {            
            return;
        } 
        if (tab==0){
            rows=this.modelTable1.getRowCount()+1;
            cols=this.modelTable1.getColumnCount();
            rowhead=new HSSFRow[rows+1] ;
            workbook = new HSSFWorkbook(); 
            sheet = workbook.createSheet("DLS vs TVD");            
        }
        rowhead[0] = sheet.createRow((short) 0);
        for (int j=0;j<=cols-1;j++) {
            rowhead[0].createCell(j).setCellValue(modelTable1.getColumnName(j));
        }        
        for (int i=1;i<=rows-1;i++) {
            row=i;
            rowhead[row] = sheet.createRow((short) row);
            for (int j=0;j<=cols-1;j++) {
                if (modelTable1.getValueAt(i-1, j)!=null)
                    rowhead[row].createCell(j).setCellValue((double) modelTable1.getValueAt(i-1, j));
            }
        }
        try {
            FileOutputStream fileOut;            
            fileOut = new FileOutputStream(archivo);
            workbook.write(fileOut);
            fileOut.close();
        } catch (Exception ex) {
                msgbox("Ocurrio un error, posiblemente el archivo de destino se encuentra abierto","Error",true);
        }        
    }
        
    public void cursorNormal(){
        this.setCursor(Cursor.getDefaultCursor());
    }
    
    public void cursorEspera(){
        this.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
    }    
    
    public void msgbox(String s, String t){
        JOptionPane.showMessageDialog(null, s,t,TrayIcon.MessageType.WARNING.ordinal());        
    }
    
    public void msgbox(String s, String t, boolean ErrorGrave){
        if (ErrorGrave)
            JOptionPane.showMessageDialog(null, s,t,TrayIcon.MessageType.ERROR.ordinal());
        else
            JOptionPane.showMessageDialog(null, s,t,TrayIcon.MessageType.WARNING.ordinal());
        
    }    
    
    public static double round(double value, int places) {
        if (places < 0) throw new IllegalArgumentException();
        long factor = (long) Math.pow(10, places);
        value = value * factor;
        long tmp = Math.round(value);
        return (double) tmp / factor;
    }
    
    public static double newDouble(String s) {
        return parseDouble(s.replace(",", "."));
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
        jComboBoxClientes = new javax.swing.JComboBox();
        jComboBoxCampo = new javax.swing.JComboBox();
        jButtonProcesar = new javax.swing.JButton();
        jButtonSalir = new javax.swing.JButton();
        jLabel1 = new javax.swing.JLabel();
        jLabel5 = new javax.swing.JLabel();
        jLabel6 = new javax.swing.JLabel();
        jComboBoxMacolla = new javax.swing.JComboBox();
        jTabbedPane1 = new javax.swing.JTabbedPane();
        jPanel1 = new javax.swing.JPanel();
        jScrollPane1 = new javax.swing.JScrollPane();
        jTable1 = new javax.swing.JTable();
        jPanel2 = new javax.swing.JPanel();
        jPanel3 = new javax.swing.JPanel();
        jButtonExportar = new javax.swing.JButton();

        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);
        setTitle("Field Summary Report");
        setMaximumSize(new java.awt.Dimension(900, 620));
        setMinimumSize(new java.awt.Dimension(900, 620));
        setPreferredSize(new java.awt.Dimension(900, 620));
        getContentPane().setLayout(new org.netbeans.lib.awtextra.AbsoluteLayout());

        jComboBoxClientes.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jComboBoxClientesActionPerformed(evt);
            }
        });
        getContentPane().add(jComboBoxClientes, new org.netbeans.lib.awtextra.AbsoluteConstraints(20, 30, 270, -1));

        jComboBoxCampo.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jComboBoxCampoActionPerformed(evt);
            }
        });
        getContentPane().add(jComboBoxCampo, new org.netbeans.lib.awtextra.AbsoluteConstraints(360, 30, 210, -1));

        jButtonProcesar.setText("Procesar");
        jButtonProcesar.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButtonProcesarActionPerformed(evt);
            }
        });
        getContentPane().add(jButtonProcesar, new org.netbeans.lib.awtextra.AbsoluteConstraints(570, 540, -1, -1));

        jButtonSalir.setText("Salir");
        jButtonSalir.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButtonSalirActionPerformed(evt);
            }
        });
        getContentPane().add(jButtonSalir, new org.netbeans.lib.awtextra.AbsoluteConstraints(780, 540, 80, -1));

        jLabel1.setText("Macolla:");
        getContentPane().add(jLabel1, new org.netbeans.lib.awtextra.AbsoluteConstraints(640, 10, 60, -1));

        jLabel5.setText("Cliente:");
        getContentPane().add(jLabel5, new org.netbeans.lib.awtextra.AbsoluteConstraints(20, 10, 60, -1));

        jLabel6.setText("Campo:");
        getContentPane().add(jLabel6, new org.netbeans.lib.awtextra.AbsoluteConstraints(360, 10, 50, -1));

        jComboBoxMacolla.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jComboBoxMacollaActionPerformed(evt);
            }
        });
        getContentPane().add(jComboBoxMacolla, new org.netbeans.lib.awtextra.AbsoluteConstraints(640, 30, 210, -1));

        jTabbedPane1.addChangeListener(new javax.swing.event.ChangeListener() {
            public void stateChanged(javax.swing.event.ChangeEvent evt) {
                jTabbedPane1StateChanged(evt);
            }
        });

        jPanel1.setLayout(new org.netbeans.lib.awtextra.AbsoluteLayout());

        jTable1.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {

            },
            new String [] {

            }
        ));
        jTable1.setAutoResizeMode(javax.swing.JTable.AUTO_RESIZE_OFF);
        jScrollPane1.setViewportView(jTable1);

        jPanel1.add(jScrollPane1, new org.netbeans.lib.awtextra.AbsoluteConstraints(0, 0, 830, 400));

        jTabbedPane1.addTab("DLS vs TVD", jPanel1);

        jPanel2.setLayout(new org.netbeans.lib.awtextra.AbsoluteLayout());
        jTabbedPane1.addTab("TVD vs % arena", jPanel2);

        jPanel3.setLayout(new org.netbeans.lib.awtextra.AbsoluteLayout());
        jTabbedPane1.addTab("TVD vs Clasificación", jPanel3);

        getContentPane().add(jTabbedPane1, new org.netbeans.lib.awtextra.AbsoluteConstraints(20, 90, 840, 430));

        jButtonExportar.setText("Exportar");
        jButtonExportar.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButtonExportarActionPerformed(evt);
            }
        });
        getContentPane().add(jButtonExportar, new org.netbeans.lib.awtextra.AbsoluteConstraints(680, 540, -1, -1));

        getAccessibleContext().setAccessibleParent(this);

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void jComboBoxClientesActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jComboBoxClientesActionPerformed
        clienteId=oManejoDeCombos.getComboID(this.jComboBoxClientes);
        String s="SELECT campoId,campoNombre from ConsultaCampoCliente1 WHERE clienteId="+clienteId;
        oManejoDeCombos.llenaCombo(oBD,oManejoDeCombos.getModeloCombo(),s,this.jComboBoxCampo,"Seleccione Campo");
        procesado=false;
    }//GEN-LAST:event_jComboBoxClientesActionPerformed

    private void jComboBoxCampoActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jComboBoxCampoActionPerformed
        campoId=oManejoDeCombos.getComboID(this.jComboBoxCampo);
        String s="SELECT macollaId,macollaNombre from ConsultaMacolla1 WHERE clienteId="+clienteId + " AND campoId="+campoId;
        oManejoDeCombos.llenaCombo(oBD,oManejoDeCombos.getModeloCombo(),s,this.jComboBoxMacolla,"Seleccione Macolla");
        procesado=false;
    }//GEN-LAST:event_jComboBoxCampoActionPerformed

    private void jButtonProcesarActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButtonProcesarActionPerformed
        procesar();
    }//GEN-LAST:event_jButtonProcesarActionPerformed

    private void jButtonSalirActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButtonSalirActionPerformed
        this.dispose();
    }//GEN-LAST:event_jButtonSalirActionPerformed

    private void jComboBoxMacollaActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jComboBoxMacollaActionPerformed
        macollaId=oManejoDeCombos.getComboID(this.jComboBoxMacolla);
        procesado=false;
    }//GEN-LAST:event_jComboBoxMacollaActionPerformed

    private void jTabbedPane1StateChanged(javax.swing.event.ChangeEvent evt) {//GEN-FIRST:event_jTabbedPane1StateChanged

    }//GEN-LAST:event_jTabbedPane1StateChanged

    private void jButtonExportarActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButtonExportarActionPerformed
        exportar(this.jTabbedPane1.getSelectedIndex());
    }//GEN-LAST:event_jButtonExportarActionPerformed

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
            java.util.logging.Logger.getLogger(Rpt005.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (InstantiationException ex) {
            java.util.logging.Logger.getLogger(Rpt005.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (IllegalAccessException ex) {
            java.util.logging.Logger.getLogger(Rpt005.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (javax.swing.UnsupportedLookAndFeelException ex) {
            java.util.logging.Logger.getLogger(Rpt005.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        }
        //</editor-fold>
        //</editor-fold>
        //</editor-fold>
        //</editor-fold>

        /* Create and display the dialog */
        java.awt.EventQueue.invokeLater(new Runnable() {
            public void run() {
                Rpt005 dialog = new Rpt005(new javax.swing.JFrame(), true,new ManejoBDAccess());
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
    private javax.swing.JButton jButtonExportar;
    private javax.swing.JButton jButtonProcesar;
    private javax.swing.JButton jButtonSalir;
    private javax.swing.JComboBox jComboBoxCampo;
    private javax.swing.JComboBox jComboBoxClientes;
    private javax.swing.JComboBox jComboBoxMacolla;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel5;
    private javax.swing.JLabel jLabel6;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JPanel jPanel3;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JTabbedPane jTabbedPane1;
    private javax.swing.JTable jTable1;
    // End of variables declaration//GEN-END:variables
}

class SumatoriaDLS{
    private double tvdDesde=0.0;
    private double tvdHasta=0.0;
    private double sumatoriaDLS=0.0;
    private double sumatoriaPorcentajeSliding=0.0;
    private int nDLS=0;
    private int nPorcentajeSliding=0;

    public SumatoriaDLS(double tvdDesde_,double tvdHasta_,String wellNombre_){
        tvdDesde=tvdDesde_;
        tvdHasta=tvdHasta_;
        sumatoriaDLS=0.0;
        sumatoriaPorcentajeSliding=0.0;
        nDLS=0;
        nPorcentajeSliding=0;
    }
    public void sumaDLS(double dls){
        if (dls>0.0) {
            sumatoriaDLS+=dls;
            nDLS++;
        }
    }
    public void sumaPorcentajeSliding(double porcentajeSliding){
        if (porcentajeSliding>0) {
            sumatoriaPorcentajeSliding+=porcentajeSliding;
            nPorcentajeSliding++;
        }
    }    
    public double getSumatoriaDLS(){
        return sumatoriaDLS;
    }
    public double getSumatoriaPorcentajeSliding(){
        return sumatoriaPorcentajeSliding;
    }
    public double getDls100(){        
        return (sumatoriaDLS/nDLS)*100/(sumatoriaPorcentajeSliding/nPorcentajeSliding);
    }
    public double getTvdDesde(){        
        return tvdDesde;
    }
    public double getTvdHasta(){        
        return tvdHasta;
    }
}

class PromedioDLS {
    private double tvdDesde;
    private double tvdHasta;
    private double sumatoriaDLSNoTurn=0;
    private double sumatoriaDLSTurnRight=0;
    private double sumatoriaDLSTurnLeft=0;   
    private double sumatoriaPorcentajeSlidingNoTurn=0;
    private double sumatoriaPorcentajeSlidingTurnRight=0;
    private double sumatoriaPorcentajeSlidingTurnLeft=0; 
    private int nNoTurn=0;
    private int nTurnRight=0;
    private int nTurnLeft=0;
    
    PromedioDLS(double tvdDesde_,double tvdHasta_){
        tvdDesde=tvdDesde_;
        tvdHasta=tvdHasta_;
    }
    
    public void suma(double dls,double porcentajeSliding, String descripcionSubSeccion){
        if (dls >0 && porcentajeSliding >0) {
            if ("Build no Turn".equals(descripcionSubSeccion)){
                   nNoTurn++;
                   sumatoriaDLSNoTurn+=dls;
                   sumatoriaPorcentajeSlidingNoTurn+=porcentajeSliding;
            } else if ("Build & Turn Right".equals(descripcionSubSeccion)){
                   nTurnRight++;
                   sumatoriaDLSTurnRight+=dls;
                   sumatoriaPorcentajeSlidingTurnRight+=porcentajeSliding;
            } else if ("Build & Turn Left".equals(descripcionSubSeccion)){
                   nTurnLeft++;
                   sumatoriaDLSTurnLeft+=dls;
                   sumatoriaPorcentajeSlidingTurnLeft+=porcentajeSliding;
            }
        }
    }
    public double getPromedioDLSNoTurn(){
        double promedio=0.0;
        if (nNoTurn>0) promedio=sumatoriaDLSNoTurn/nNoTurn;
        return promedio;
    }
    public double getPromedioDLSTurnRight(){
        double promedio=0.0;
        if (nTurnRight>0) promedio=sumatoriaDLSTurnRight/nTurnRight;
        return promedio;
    }
    public double getPromedioDLSTurnLeft(){
        double promedio=0.0;
        if (nTurnLeft>0) promedio=sumatoriaDLSTurnLeft/nTurnLeft;
        return promedio;
    }
    
    public double getPromedioporcentajeSlidingNoTurn(){
        double promedio=0.0;
        if (nNoTurn>0) promedio=sumatoriaPorcentajeSlidingNoTurn/nNoTurn;
        return promedio;
    }
    public double getPromedioporcentajeSlidingTurnRight(){
        double promedio=0.0;
        if (nTurnRight>0) promedio=sumatoriaPorcentajeSlidingTurnRight/nTurnRight;
        return promedio;
    }
    public double getPromedioporcentajeSlidingTurnLeft(){
        double promedio=0.0;
        if (nTurnLeft>0) promedio=sumatoriaPorcentajeSlidingTurnLeft/nTurnLeft;
        return promedio;
    }
    
    public double getDls100NoTurn(){
        if (nNoTurn>0)
            return (sumatoriaDLSNoTurn/nNoTurn)*100/(sumatoriaPorcentajeSlidingNoTurn/nNoTurn);
        else return 0;
    }
    public double getDls100TurnRight(){ 
        if (nNoTurn>0)
            return (sumatoriaDLSTurnRight/nNoTurn)*100/(sumatoriaPorcentajeSlidingTurnRight/nNoTurn);
        else return 0;
    }  
    public double getDls100TurnLeft(){
        if (nNoTurn>0)        
            return (sumatoriaDLSTurnLeft/nNoTurn)*100/(sumatoriaPorcentajeSlidingTurnLeft/nNoTurn);
        else return 0;
    } 
}

