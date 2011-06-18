package net.sf.openrocket.gui.configdialog;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSpinner;
import javax.swing.SwingUtilities;

import net.miginfocom.swing.MigLayout;
import net.sf.openrocket.gui.SpinnerEditor;
import net.sf.openrocket.gui.adaptors.DoubleModel;
import net.sf.openrocket.gui.adaptors.EnumModel;
import net.sf.openrocket.gui.components.BasicSlider;
import net.sf.openrocket.gui.components.StyledLabel;
import net.sf.openrocket.gui.components.StyledLabel.Style;
import net.sf.openrocket.gui.components.UnitSelector;
import net.sf.openrocket.l10n.Translator;
import net.sf.openrocket.logging.LogHelper;
import net.sf.openrocket.rocketcomponent.FinSet;
import net.sf.openrocket.rocketcomponent.FreeformFinSet;
import net.sf.openrocket.rocketcomponent.RocketComponent;
import net.sf.openrocket.startup.Application;
import net.sf.openrocket.unit.UnitGroup;


public abstract class FinSetConfig extends RocketComponentConfig {
	private static final LogHelper log = Application.getLogger();
	private static final Translator trans = Application.getTranslator();

	private JButton split = null;
	
	public FinSetConfig(RocketComponent component) {
		super(component);
		
		//// Fin tabs and Through-the-wall fin tabs
		tabbedPane.insertTab(trans.get("FinSetConfig.tab.Fintabs"), null, finTabPanel(), 
				trans.get("FinSetConfig.tab.Through-the-wall"), 0);
	}
	
	

	protected void addFinSetButtons() {
		JButton convert = null;
		
		//// Convert buttons
		if (!(component instanceof FreeformFinSet)) {
			//// Convert to freeform
			convert = new JButton(trans.get("FinSetConfig.but.Converttofreeform"));
			//// Convert this fin set into a freeform fin set
			convert.setToolTipText(trans.get("FinSetConfig.but.Converttofreeform.ttip"));
			convert.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					log.user("Converting " + component.getComponentName() + " into freeform fin set");
					
					// Do change in future for overall safety
					SwingUtilities.invokeLater(new Runnable() {
						@Override
						public void run() {
							//// Convert fin set
							ComponentConfigDialog.addUndoPosition(trans.get("FinSetConfig.Convertfinset"));
							RocketComponent freeform =
									FreeformFinSet.convertFinSet((FinSet) component);
							ComponentConfigDialog.showDialog(freeform);
						}
					});
					
					ComponentConfigDialog.hideDialog();
				}
			});
		}
		
		//// Split fins
		split = new JButton(trans.get("FinSetConfig.but.Splitfins"));
		//// Split the fin set into separate fins
		split.setToolTipText(trans.get("FinSetConfig.but.Splitfins.ttip"));
		split.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				log.user("Splitting " + component.getComponentName() + " into separate fins, fin count=" +
						((FinSet) component).getFinCount());
				
				// Do change in future for overall safety
				SwingUtilities.invokeLater(new Runnable() {
					@Override
					public void run() {
						RocketComponent parent = component.getParent();
						int index = parent.getChildPosition(component);
						int count = ((FinSet) component).getFinCount();
						double base = ((FinSet) component).getBaseRotation();
						if (count <= 1)
							return;
						
						ComponentConfigDialog.addUndoPosition("Split fin set");
						parent.removeChild(index);
						for (int i = 0; i < count; i++) {
							FinSet copy = (FinSet) component.copy();
							copy.setFinCount(1);
							copy.setBaseRotation(base + i * 2 * Math.PI / count);
							copy.setName(copy.getName() + " #" + (i + 1));
							parent.addChild(copy, index + i);
						}
					}
				});
				
				ComponentConfigDialog.hideDialog();
			}
		});
		split.setEnabled(((FinSet) component).getFinCount() > 1);
		
		if (convert == null)
			addButtons(split);
		else
			addButtons(split, convert);
		
	}
	
	public JPanel finTabPanel() {
		JPanel panel = new JPanel(
				new MigLayout("align 50% 20%, fillx, gap rel unrel, ins 20lp 10% 20lp 10%",
						"[150lp::][65lp::][30lp::][200lp::]", ""));
		//		JPanel panel = new JPanel(new MigLayout("fillx, align 20% 20%, gap rel unrel",
		//				"[40lp][80lp::][30lp::][100lp::]",""));
		
		//// Through-the-wall fin tabs:
		panel.add(new StyledLabel(trans.get("FinSetConfig.lbl.Through-the-wall"), Style.BOLD),
				"spanx, wrap 30lp");
		
		JLabel label;
		DoubleModel m;
		DoubleModel length;
		DoubleModel length2;
		DoubleModel length_2;
		JSpinner spin;
		
		length = new DoubleModel(component, "Length", UnitGroup.UNITS_LENGTH, 0);
		length2 = new DoubleModel(component, "Length", 0.5, UnitGroup.UNITS_LENGTH, 0);
		length_2 = new DoubleModel(component, "Length", -0.5, UnitGroup.UNITS_LENGTH, 0);
		
		register(length);
		register(length2);
		register(length_2);
		
		////  Tab length
		//// Tab length:
		label = new JLabel(trans.get("FinSetConfig.lbl.Tablength"));
		//// The length of the fin tab.
		label.setToolTipText(trans.get("FinSetConfig.ttip.Tablength"));
		panel.add(label, "gapleft para, gapright 40lp, growx 1");
		
		m = new DoubleModel(component, "TabLength", UnitGroup.UNITS_LENGTH, 0);
		
		spin = new JSpinner(m.getSpinnerModel());
		spin.setEditor(new SpinnerEditor(spin));
		panel.add(spin, "growx 1");
		
		panel.add(new UnitSelector(m), "growx 1");
		panel.add(new BasicSlider(m.getSliderModel(DoubleModel.ZERO, length)),
				"w 100lp, growx 5, wrap");
		

		////  Tab length
		//// Tab height:
		label = new JLabel(trans.get("FinSetConfig.lbl.Tabheight"));
		//// The spanwise height of the fin tab.
		label.setToolTipText(trans.get("FinSetConfig.ttip.Tabheight"));
		panel.add(label, "gapleft para");
		
		m = new DoubleModel(component, "TabHeight", UnitGroup.UNITS_LENGTH, 0);
		
		spin = new JSpinner(m.getSpinnerModel());
		spin.setEditor(new SpinnerEditor(spin));
		panel.add(spin, "growx");
		
		panel.add(new UnitSelector(m), "growx");
		panel.add(new BasicSlider(m.getSliderModel(DoubleModel.ZERO, length2)),
				"w 100lp, growx 5, wrap para");
		

		////  Tab position:
		label = new JLabel(trans.get("FinSetConfig.lbl.Tabposition"));
		//// The position of the fin tab.
		label.setToolTipText(trans.get("FinSetConfig.ttip.Tabposition"));
		panel.add(label, "gapleft para");
		
		m = new DoubleModel(component, "TabShift", UnitGroup.UNITS_LENGTH);
		
		spin = new JSpinner(m.getSpinnerModel());
		spin.setEditor(new SpinnerEditor(spin));
		panel.add(spin, "growx");
		
		panel.add(new UnitSelector(m), "growx");
		panel.add(new BasicSlider(m.getSliderModel(length_2, length2)), "w 100lp, growx 5, wrap");
		

		//// relative to
		label = new JLabel(trans.get("FinSetConfig.lbl.relativeto"));
		panel.add(label, "right, gapright unrel");
		
		EnumModel<FinSet.TabRelativePosition> em =
				new EnumModel<FinSet.TabRelativePosition>(component, "TabRelativePosition");
		
		panel.add(new JComboBox(em), "spanx 3, growx");
		
		return panel;
	}
	
	@Override
	public void updateFields() {
		super.updateFields();
		if (split != null)
			split.setEnabled(((FinSet) component).getFinCount() > 1);
	}
}
