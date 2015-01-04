package editor.ui;

import editor.data.OptionFile;
import editor.data.Player;
import editor.data.Stats;
import editor.util.Bits;
import editor.util.Resources;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class PlayerDialog extends JDialog {
	private final OptionFile of;
	private final PlayerImportDialog playerImportDia;

	private volatile int index;

	private GeneralAbilityPanel generalPan;
	private PositionPanel positionPan;
	private Ability99Panel abilityPan;
	private SpecialAbilityPanel specialPan;
	private JButton importButton;

	public PlayerDialog(Frame owner, OptionFile of, PlayerImportDialog pid) {
		super(owner, Resources.getMessage("player.title"), true);
		if (null == of) throw new NullPointerException("of");
		if (null == pid) throw new NullPointerException("pid");
		this.of = of;
		playerImportDia = pid;

		initComponents();
	}

	private void initComponents() {
		JButton acceptButton = new JButton(Resources.getMessage("player.accept"));
		acceptButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent evt) {
				if (isFormValid()) {
					updateStats();
					setVisible(false);
				}
			}
		});

		CancelButton cancelButton = new CancelButton(this);

		importButton = new JButton(Resources.getMessage("player.import"));
		importButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent evt) {
				playerImportDia.show(index);
				setVisible(false);
			}
		});

		generalPan = new GeneralAbilityPanel(of);
		positionPan = new PositionPanel(of);
		abilityPan = new Ability99Panel(of);
		specialPan = new SpecialAbilityPanel(of);

		JPanel bottomPane = new JPanel();
		bottomPane.add(acceptButton);
		bottomPane.add(cancelButton);
		bottomPane.add(importButton);

		JPanel leftPane = new JPanel(new BorderLayout());
		leftPane.add(generalPan, BorderLayout.NORTH);
		leftPane.add(positionPan, BorderLayout.CENTER);
		leftPane.add(bottomPane, BorderLayout.SOUTH);

		JPanel contentPane = new JPanel();
		contentPane.add(leftPane);
		contentPane.add(abilityPan);
		contentPane.add(specialPan);
		getContentPane().add(contentPane);

		setResizable(false);
		pack();
	}

	public GeneralAbilityPanel getGeneralPan() {
		return generalPan;
	}

	public void show(Player player) {
		if (null == player) throw new NullPointerException("player");
		index = player.getIndex();

		String tit = String.format("[%d] %s", index, player.getName());
		tit = Resources.getMessage("title.format", Resources.getMessage("player.title"), tit);
		setTitle(tit);

		importButton.setVisible(playerImportDia.isOf2Loaded());
		generalPan.load(index);
		positionPan.load(index);
		abilityPan.load(index);
		specialPan.load(index);

		setVisible(true);
	}

	private boolean isFormValid() {
		InputVerifier verifier = new Ability99Panel.Verifier99();
		for (int i = 0; i < Stats.ABILITY99.length; i++) {
			if (!verifier.verify(abilityPan.getField(i)))
				return false;
		}

		verifier = new GeneralAbilityPanel.StatVerifier(Stats.HEIGHT);
		if (!verifier.verify(generalPan.getHeightField()))
			return false;

		verifier = new GeneralAbilityPanel.StatVerifier(Stats.WEIGHT);
		if (!verifier.verify(generalPan.getWeightField()))
			return false;

		verifier = new GeneralAbilityPanel.StatVerifier(Stats.AGE);
		if (!verifier.verify(generalPan.getAgeField()))
			return false;

		return true;
	}

	private void updateStats() {
		for (int i = 0; i < Stats.ROLES.length; i++) {
			Stats.setValue(of, index, Stats.ROLES[i], Bits.toByte(positionPan.getRoleCheck(i).isSelected()));
		}

		int regRole = 0;
		for (int i = 0; i < Stats.ROLES.length; i++) {
			if (Stats.ROLES[i].getName().equalsIgnoreCase(positionPan.getRegBox().getSelectedItem())) {
				regRole = i;
				break;
			}
		}
		Stats.setValue(of, index, Stats.REG_POS, regRole);

		Stats.setValue(of, index, Stats.HEIGHT, generalPan.getHeightField().getText());

		int footIdx = generalPan.getFootBox().getSelectedIndex();
		int foot = footIdx / 3;
		int side = footIdx - foot * 3;
		Stats.setValue(of, index, Stats.FOOT, foot);
		Stats.setValue(of, index, Stats.FAVORITE_SIDE, side);

		Stats.setValue(of, index, Stats.WEAK_FOOT_ACC, generalPan.getWeakFootAccBox().getSelectedItem());
		Stats.setValue(of, index, Stats.WEAK_FOOT_FREQ, generalPan.getWeakFootFreqBox().getSelectedItem());

		for (int i = 0; i < Stats.ABILITY99.length; i++) {
			Stats.setValue(of, index, Stats.ABILITY99[i], abilityPan.getField(i).getText());
		}

		Stats.setValue(of, index, Stats.CONSISTENCY, generalPan.getConsistencyBox().getSelectedItem());
		Stats.setValue(of, index, Stats.CONDITION, generalPan.getConditionBox().getSelectedItem());

		for (int i = 0; i < Stats.ABILITY_SPECIAL.length; i++) {
			Stats.setValue(of, index, Stats.ABILITY_SPECIAL[i],
					Bits.toByte(specialPan.getAbilityCheck(i).isSelected()));
		}

		Stats.setValue(of, index, Stats.INJURY, generalPan.getInjuryBox().getSelectedItem());
		Stats.setValue(of, index, Stats.FREE_KICK, generalPan.getFreeKickBox().getSelectedItem());
		Stats.setValue(of, index, Stats.PK_STYLE, generalPan.getPenaltyBox().getSelectedItem());
		Stats.setValue(of, index, Stats.AGE, generalPan.getAgeField().getText());
		Stats.setValue(of, index, Stats.WEIGHT, generalPan.getWeightField().getText());
		Stats.setValue(of, index, Stats.NATIONALITY, generalPan.getNationBox().getSelectedItem());
		Stats.setValue(of, index, Stats.DRIBBLE_STYLE, generalPan.getDribbleBox().getSelectedItem());
		Stats.setValue(of, index, Stats.DK_STYLE, generalPan.getDropKickBox().getSelectedItem());

		Stats.setValue(of, index, Stats.ABILITY_EDITED, 1);
	}

}
