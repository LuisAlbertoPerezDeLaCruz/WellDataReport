/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package my;

import java.awt.Color;
import java.awt.Component;
import javax.swing.JLabel;
import javax.swing.JTable;
import javax.swing.table.DefaultTableCellRenderer;

/**
 *
 * @author Luis
 */
public class MyRendererP40h  extends DefaultTableCellRenderer {
    @Override
    public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int col) {

      //Cells are by default rendered as a JLabel.
      JLabel l = (JLabel) super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, col);

      //Get the status for the current row.
      String s=table.getValueAt(row, 2).toString();
      Double d=0.0;
      if (! s.isEmpty()) {
        d =Double.parseDouble(s);
        if (d <= 20.0) {
          l.setBackground(Color.blue);
          l.setForeground(Color.WHITE);          
        } else {
          l.setBackground(Color.GREEN);
          l.setForeground(Color.black);
        }
      } else
        l.setBackground(Color.WHITE);


    //Return the JLabel which renders the cell.
    return l;

   }
}
