package bitSlicer.gui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.io.File;
import java.lang.reflect.Field;
import java.util.HashSet;
import java.util.Vector;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.DefaultListCellRenderer;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSpinner;
import javax.swing.JTabbedPane;
import javax.swing.JTextArea;
import javax.swing.SpinnerNumberModel;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.filechooser.FileFilter;

import bitSlicer.BitSlicerMain;
import bitSlicer.Slicer.Config.CraftConfig;
import bitSlicer.Slicer.Config.CraftConfigLoader;
import bitSlicer.Slicer.Config.Setting;
import bitSlicer.util.Logger;

/**
 * The ConfigWindow class generates a JFrame window with the configurable options.
 * 
 * It uses reflection to get the configurable settings. This makes adding new settings easy.
 * 
 * NOTE: I suck at UI coding.
 */
public class ConfigWindow extends JFrame
{
	private static final long serialVersionUID = 1L;

	private JPanel configSettingsPanel;
	private JPanel actionPanel;

	public ConfigWindow()
	{
		this.setTitle("BitSlicer - " + CraftConfig.VERSION);
		this.setResizable(false);
		this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

		JTabbedPane tabbedPane = new JTabbedPane();
		this.configSettingsPanel = new JPanel();
		this.configSettingsPanel.setLayout(new BoxLayout(this.configSettingsPanel, BoxLayout.Y_AXIS));
		this.actionPanel = new JPanel(new GridBagLayout());

		tabbedPane.addTab("Settings", this.configSettingsPanel);

		GridBagConstraints c = new GridBagConstraints();
		c.gridy = 0;
		c.anchor = GridBagConstraints.WEST;
		c.insets = new Insets(1, 1, 1, 1);
		c.fill = GridBagConstraints.HORIZONTAL;

		JButton sliceButton = new JButton("Slice");
		sliceButton.addActionListener(new ActionListener()
		{
			public void actionPerformed(ActionEvent e)
			{
				final JFileChooser fc = new JFileChooser();
				fc.setFileFilter(new FileFilter()
				{
					public boolean accept(File f)
					{
						if (f.isDirectory())
							return true;
						return f.getName().endsWith(".stl");
					}

					public String getDescription()
					{
						return null;
					}

				});
				fc.setSelectedFile(new File(CraftConfig.lastSlicedFile));
				int returnVal = fc.showOpenDialog(null);
				if (returnVal == JFileChooser.APPROVE_OPTION)
				{
					final LogWindow logWindow = new LogWindow();
					new Thread(new Runnable()
					{
						public void run()
						{
							try
							{
								BitSlicerMain.sliceModel(fc.getSelectedFile().toString());
								logWindow.dispose();
							} catch (Exception e)
							{
								e.printStackTrace();
								logWindow.dispose();
								StringBuilder sb = new StringBuilder();
								sb.append(e.toString());
								sb.append("\n");
								for (StackTraceElement el : e.getStackTrace())
								{
									sb.append(el.toString());
									sb.append("\n");
								}
								JOptionPane.showMessageDialog(null, sb, "Exception", JOptionPane.ERROR_MESSAGE);
							}
						}
					}).start();
					ConfigWindow.this.dispose();
				}
			}
		});
		this.actionPanel.add(sliceButton, c);

		this.add(tabbedPane);
		this.add(actionPanel, BorderLayout.SOUTH);

		createConfigFields();
		this.setVisible(true);
		System.out.println("Waiting for the user");
	}

	private void createConfigFields()
	{
		configSettingsPanel.removeAll();

		HashSet<String> doneGroups = new HashSet<String>();

		for (final Field f : CraftConfig.class.getFields())
		{
			final Setting s = f.getAnnotation(Setting.class);
			if (s == null)
				continue;
			if (doneGroups.contains(s.group()))
				continue;
			doneGroups.add(s.group());
			JPanel p = new JPanel(new GridBagLayout());
			p.setBorder(BorderFactory.createTitledBorder(s.group()));
			if (addConfigFields(p, s.group()) > 0)
				configSettingsPanel.add(p);
		}

		this.pack();
		this.setLocationRelativeTo(null);
	}

	private int addConfigFields(JPanel p, String groupName)
	{
		GridBagConstraints c = new GridBagConstraints();
		c.gridy = 0;
		c.anchor = GridBagConstraints.WEST;
		c.insets = new Insets(1, 1, 1, 1);
		c.fill = GridBagConstraints.HORIZONTAL;
		c.weighty = 1;
		for (final Field f : CraftConfig.class.getFields())
		{
			final Setting s = f.getAnnotation(Setting.class);
			Object obj = null;

			try
			{
				obj = f.get(null).toString();

				if (s == null || obj == null)
					continue;
				if (!s.group().equals(groupName))
					continue;
				final Component comp = getSwingComponentForField(f, s);

				if (comp == null)
					continue;

				final JLabel label = new JLabel(s.title() + ":");
				JButton helpButton = null;

				if (!s.description().equals(""))
				{
					helpButton = new JButton("?");
					helpButton.setMargin(new java.awt.Insets(0, 1, 0, 1));
					helpButton.addActionListener(new ActionListener()
					{
						public void actionPerformed(ActionEvent e)
						{
							JOptionPane.showMessageDialog(label, s.description());
						}
					});
				}

				comp.setPreferredSize(new Dimension(100, 25));
				c.weightx = 0;
				c.gridx = 0;
				p.add(helpButton, c);
				c.weightx = 1;
				c.gridx = 1;
				p.add(label, c);
				c.gridx = 2;
				p.add(comp, c);
				c.gridy++;
			} catch (IllegalArgumentException e)
			{
				e.printStackTrace();
			} catch (IllegalAccessException e)
			{
				e.printStackTrace();
			}
		}
		return c.gridy;
	}

	private Component getSwingComponentForField(final Field f, Setting s) throws IllegalArgumentException, IllegalAccessException
	{
		if (f.getType() == Integer.TYPE)
		{
			if (s.enumName().equals(""))
			{
				JSpinner spinner = new JSpinner(new SpinnerNumberModel(f.getInt(null), (int) s.minValue(), (int) s.maxValue(), 1));
				spinner.addChangeListener(new ChangeListener()
				{
					public void stateChanged(ChangeEvent e)
					{
						try
						{
							f.setInt(null, ((Integer) ((JSpinner) e.getSource()).getValue()).intValue());
							CraftConfigLoader.saveConfig(null);
						} catch (Exception e1)
						{
							e1.printStackTrace();
						}
					}
				});
				return spinner;
			} else
			{
				Vector<String> items = new Vector<String>();
				for (final Field enumField : CraftConfig.class.getFields())
				{
					String name = enumField.getName();
					if (name.startsWith(s.enumName() + "_"))
					{
						name = name.substring(name.indexOf("_") + 1);
						name = name.substring(0, 1).toUpperCase() + name.substring(1).toLowerCase();
						name = name.replace('_', ' ');
						items.add(name);
					}
				}
				final JComboBox combo = new JComboBox(items);
				combo.setSelectedIndex(f.getInt(null));
				combo.addActionListener(new ActionListener()
				{
					public void actionPerformed(ActionEvent e)
					{
						try
						{
							f.setInt(null, combo.getSelectedIndex());
							CraftConfigLoader.saveConfig(null);
						} catch (Exception e1)
						{
							e1.printStackTrace();
						}
					}
				});
				return combo;
			}
		} else if (f.getType() == Double.TYPE)
		{
			JSpinner spinner = new JSpinner(new SpinnerNumberModel(f.getDouble(null), s.minValue(), s.maxValue(), 0.01));
			spinner.addChangeListener(new ChangeListener()
			{
				public void stateChanged(ChangeEvent e)
				{
					try
					{
						f.setDouble(null, ((Double) ((JSpinner) e.getSource()).getValue()).doubleValue());
						CraftConfigLoader.saveConfig(null);
					} catch (Exception e1)
					{
						e1.printStackTrace();
					}
				}
			});
			return spinner;
		} else if (f.getType() == Boolean.TYPE)
		{
			JCheckBox checkbox = new JCheckBox();
			checkbox.setSelected(f.getBoolean(null));
			checkbox.addActionListener(new ActionListener()
			{
				public void actionPerformed(ActionEvent e)
				{
					try
					{
						f.setBoolean(null, ((JCheckBox) e.getSource()).isSelected());
						CraftConfigLoader.saveConfig(null);
					} catch (Exception e1)
					{
						e1.printStackTrace();
					}
				}
			});
			return checkbox;
		} else
		{
			//Logger.error("Unknown field type for config window: " + f.getType());
		}
		return null;
	}
}