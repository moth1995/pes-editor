package editor.ui;

import editor.data.Logos;
import editor.data.OptionFile;
import editor.util.*;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.image.BufferedImage;
import java.awt.image.ColorModel;
import java.awt.image.IndexColorModel;
import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;

public class LogoPanel extends JPanel implements ActionListener {
	private final OptionFile of;
	private final LogoImportDialog logoImportDia;

	private volatile boolean isTrans = true;

	public LogoPanel(OptionFile of, LogoImportDialog lid) {
		super();
		if (null == of) throw new NullPointerException("of");
		if (null == lid) throw new NullPointerException("lid");
		this.of = of;
		logoImportDia = lid;

		initComponents();
		refresh();
	}

	private JFileChooser chooser;
	private JFileChooser pngChooser;
	private final JButton[] flagButtons = new JButton[Logos.TOTAL];

	private void initComponents() {
		ImageFileFilter filter = new ImageFileFilter();
		chooser = new JFileChooser();
		chooser.addChoosableFileFilter(filter);
		chooser.setAcceptAllFileFilterUsed(false);
		chooser.setDialogTitle(Resources.getMessage("logo.import"));

		PngFilter pngFilter = new PngFilter();
		pngChooser = new JFileChooser();
		pngChooser.addChoosableFileFilter(pngFilter);
		pngChooser.setAcceptAllFileFilterUsed(false);
		pngChooser.setDialogTitle(Resources.getMessage("logo.export"));

		JPanel flagPanel = new JPanel(new GridLayout(8, 10));
		int iconSize = Math.round(1.2f * Logos.IMG_SIZE);

		Systems.javaUI();// fix button background color
		for (int l = 0; l < flagButtons.length; l++) {
			flagButtons[l] = new JButton();
			flagButtons[l].setBackground(Colors.GRAY80);
			flagButtons[l].setMargin(new Insets(0, 0, 0, 0));
			flagButtons[l].setPreferredSize(new Dimension(iconSize, iconSize));
			flagButtons[l].setActionCommand(Integer.toString(l));
			flagButtons[l].addActionListener(this);

			flagPanel.add(flagButtons[l]);
		}
		Systems.systemUI();

		JButton transButton = new JButton(Resources.getMessage("Transparency"));
		transButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent evt) {
				onTransparency(evt);
			}
		});

		JPanel contentPane = new JPanel(new BorderLayout());
		contentPane.add(flagPanel, BorderLayout.CENTER);
		contentPane.add(transButton, BorderLayout.SOUTH);

		add(contentPane);
	}

	private void onTransparency(ActionEvent evt) {
		isTrans = !isTrans;
		refresh();
	}

	public void actionPerformed(ActionEvent evt) {
		if (null == evt) throw new NullPointerException("evt");
		if (!(evt.getSource() instanceof AbstractButton)) throw new IllegalArgumentException("evt");

		AbstractButton btn = (AbstractButton) evt.getSource();
		int slot = Integer.parseInt(btn.getActionCommand());
		ImageIcon icon = new ImageIcon(Logos.get(of, slot, !isTrans));

		Object[] opts = getOptions(logoImportDia.isOf2Loaded());
		int returnVal = JOptionPane.showOptionDialog(null,
				Resources.getMessage("logo.title"), Resources.getMessage("logo.label"),
				JOptionPane.YES_NO_CANCEL_OPTION, JOptionPane.QUESTION_MESSAGE, icon, opts, opts[0]);

		switch (returnVal) {
			case JOptionPane.YES_OPTION:
				importLogo(slot);
				break;
			case JOptionPane.NO_OPTION:
				if (Logos.isUsed(of, slot))
					saveLogoAsPNG(slot);
				break;
			default:// JOptionPane.CANCEL_OPTION:
				if (logoImportDia.isOf2Loaded())
					importFromOF2(slot);
				break;
		}
	}

	private static Object[] getOptions(boolean of2Loaded) {
		String s = Resources.getMessage("logo.options");
		String[] opts = s.split("\\s*,\\s*");
		if (of2Loaded || opts.length < 2)
			return opts;

		ArrayList<String> arr = new ArrayList<String>(Arrays.asList(opts));
		arr.remove(arr.size() - 2);
		return arr.toArray();
	}

	private void importFromOF2(int slot) {
		logoImportDia.show(slot, Resources.getMessage("logo.import"));
		refresh(slot);
	}

	private void importLogo(int slot) {
		int returnVal = chooser.showOpenDialog(null);
		if (returnVal != JFileChooser.APPROVE_OPTION)
			return;

		try {
			File source = chooser.getSelectedFile();
			BufferedImage image = ImageIO.read(source);

			validateImage(image);
			Logos.set(of, slot, image);
			refresh(slot);
		} catch (Exception e) {
			showAccessFailedMsg(e.getLocalizedMessage());
		}
	}

	private void saveLogoAsPNG(int slot) {
		int returnVal = pngChooser.showSaveDialog(null);
		if (returnVal != JFileChooser.APPROVE_OPTION)
			return;

		File dest = pngChooser.getSelectedFile();
		dest = Files.addExtension(dest, Files.PNG);

		if (dest.exists()) {
			returnVal = JOptionPane.showConfirmDialog(null,
					Resources.getMessage("msg.overwrite", dest.getName(), dest.getParent()),
					Resources.getMessage("msg.overwrite.title", dest.getName()),
					JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE, null);

			if (returnVal != JOptionPane.YES_OPTION) {
				return;
			} else if (!dest.delete()) {
				showAccessFailedMsg(null);
				return;
			}
		}

		writeFile(dest, slot);
	}

	private void writeFile(File dest, int slot) {
		try {
			BufferedImage image = Logos.get(of, slot, false);
			if (ImageIO.write(image, Files.PNG, dest)) {
				JOptionPane.showMessageDialog(null,
						Resources.getMessage("msg.saveSuccess", dest.getName(), dest.getParent()),
						Resources.getMessage("msg.saveSuccess.title"), JOptionPane.INFORMATION_MESSAGE);
			}
		} catch (Exception e) {
			showAccessFailedMsg(e.getLocalizedMessage());
		}
	}

	private void refresh(int slot) {
		Image icon = Logos.get(of, slot, !isTrans);
		flagButtons[slot].setIcon(new ImageIcon(icon));
	}

	public void refresh() {
		for (int l = 0; l < Logos.TOTAL; l++) {
			refresh(l);
		}
	}

	private static void validateImage(BufferedImage image) {
		if (null == image) throw new NullPointerException("image");

		if (image.getWidth() != Logos.IMG_SIZE || image.getHeight() != Logos.IMG_SIZE)
			throw new IllegalArgumentException(Resources.getMessage("msg.invalidSize", Logos.IMG_SIZE, Logos.IMG_SIZE));

		ColorModel colorMod = image.getColorModel();
		if (null == colorMod || !(colorMod instanceof IndexColorModel))
			throw new IllegalArgumentException(Resources.getMessage("msg.notIndexed"));

		int paletteSize = Images.paletteSize(Logos.BITS_DEPTH);
		if (((IndexColorModel) colorMod).getMapSize() > paletteSize)
			throw new IllegalArgumentException(Resources.getMessage("msg.manyColors", paletteSize));
	}

	private static void showAccessFailedMsg(String msg) {
		if (Strings.isBlank(msg)) msg = Resources.getMessage("msg.accessFailed");
		JOptionPane.showMessageDialog(null, msg, Resources.getMessage("Error"), JOptionPane.ERROR_MESSAGE);
	}

}
