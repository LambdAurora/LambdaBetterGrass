/*
 * Copyright © 2023 LambdAurora <email@lambdaurora.dev>
 *
 * This file is part of LambdaBetterGrass.
 *
 * Licensed under the MIT license. For more information,
 * see the LICENSE file.
 */

package dev.lambdaurora.lambdabettergrass;

import net.fabricmc.loader.api.entrypoint.PreLaunchEntrypoint;

import javax.imageio.ImageIO;
import javax.swing.*;
import javax.swing.event.HyperlinkEvent;
import java.awt.*;
import java.awt.geom.AffineTransform;
import java.awt.image.AffineTransformOp;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Objects;

@SuppressWarnings("deprecation")
public final class MigrationNotice implements PreLaunchEntrypoint {
	static {
		if (System.getProperty("awt.useSystemAAFontSettings") == null) System.setProperty("awt.useSystemAAFontSettings", "on");
		if (System.getProperty("swing.aatext") == null) System.setProperty("swing.aatext", "true");
	}

	private final JFrame frame = new JFrame("LambdaBetterGrass Migration Notice");
	private final Image iconImage = rescaled(ImageIO.read(Objects.requireNonNull(MigrationNotice.class.getResource("/assets/lambdabettergrass/icon.png"))), 4);
	private final Icon icon = new ImageIcon(this.iconImage);

	public MigrationNotice() throws IOException {}

	@Override
	public void onPreLaunch() {
		var scrollPane = new JScrollPane(this.makeTextPanel(), JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED, JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);

		var panel = new JPanel();
		panel.setLayout(new BoxLayout(panel, BoxLayout.PAGE_AXIS));

		panel.setBorder(BorderFactory.createEmptyBorder());
		panel.add(scrollPane, BorderLayout.CENTER);

		var buttons = new JPanel();
		var goToQuiltButton = new JButton("Install Quilt");
		goToQuiltButton.addActionListener(e -> {
			try {
				Desktop.getDesktop().browse(new URI("https://quiltmc.org/en/install/"));
			} catch (IOException | UnsupportedOperationException ex) {
				JOptionPane.showMessageDialog(
						this.frame,
						"It seems there's difficulty in opening the browser. Please copy and open this link: https://quiltmc.org/en/install/",
						"Failed to open link in browser",
						JOptionPane.WARNING_MESSAGE,
						this.icon
				);
			} catch (URISyntaxException ex) {
				// Ignored.
			}
		});
		buttons.add(goToQuiltButton);
		var closeButton = new JButton("Close");
		closeButton.addActionListener(e -> this.frame.dispose());
		buttons.add(closeButton);
		panel.add(buttons, BorderLayout.SOUTH);

		this.frame.setContentPane(panel);

		this.frame.setIconImage(this.iconImage);
		this.frame.pack();
		this.frame.setVisible(true);
		this.frame.setMaximumSize(new Dimension(900, 700));
		this.frame.setMaximizedBounds(new Rectangle(900, 700));
		this.frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
	}

	private JComponent makeTextPanel() {
		var textArea = new JTextPane();
		textArea.setContentType("text/html");
		textArea.setText("""
				<html>
				<h1>LambdaBetterGrass is moving to Quilt!</h1>
				<p>You heard right, the mod is moving to the <a href="https://quiltmc.org/">Quilt</a> mod loader.</p>
				<br />
				<h3>Why move to Quilt?</h3>
				<p>This mod is heavily relying on modifying resource loading mechanics, mostly by injecting dynamically generated resources.
				Minecraft 1.19.3 came around, despite not being the most feature-filled for users, it's filled with refactors, including stuff touching resource loading.</p>
				<p>For my mods, despite being directly involved with Quilt's development, I have decided to keep using Fabric as long as it's not a burden.
				But with those resource loading refactors, the port burden was very high, and Quilt now has the resource loading APIs I need for this mod.
				Which is why I moved this mod to it, to ease the port to 1.19.3 and hopefully reduce maintenance burden in the future.</p>
				<p>As a bonus, moving to Quilt also fixes the default resource pack being enabled on first installation, which was intended but not working on Fabric
				due to it not being fully implemented.</p>
				<br />
				<h3>Can I still use the mod with Fabric mods?</h3>
				<p>Yes, absolutely!</p>
				<p>It's one of the big advantages of Quilt, mods switching to it can benefit greatly from the new APIs while letting the users continue to use
				their favorite Fabric mods alongside.</p>
				<br />
				<h3>What about CurseForge modpacks?</h3>
				<p>CurseForge not having support for Quilt modpacks at the time of writing, a forgotten mod has been dusted off and adapted to allow
				the usage of Quilt in CurseForge modpacks.<br/>
				Indeed, the remnants of JumpLoader have been picked up to become <a href="https://www.curseforge.com/minecraft/mc-mods/jumpquilt">JumpQuilt</a>.</p>
				<br />
				<h3>Having challenges getting Quilt working?</h3>
				<p>Feel free to join the <a href="https://discord.quiltmc.org/">Quilt Discord</a> or the <a href="https://forum.quiltmc.org/">Quilt Forum</a>
				to get support from experienced users and developers.</p>
				<p>Having trouble with LambdaBetterGrass specifically? Feel free <a href="https://github.com/LambdAurora/LambdaBetterGrass/issues">to make an issue</a>
				or join my <a href="https://discord.gg/abEbzzv">Discord guild</a>.</p>
				</html>
				""");
		textArea.setEditable(false);
		textArea.addHyperlinkListener(e -> {
			if (e.getEventType() != HyperlinkEvent.EventType.ACTIVATED) return;
			try {
				Desktop.getDesktop().browse(e.getURL().toURI());
			} catch (IOException | UnsupportedOperationException ex) {
				JOptionPane.showMessageDialog(
						this.frame,
						"It seems there's difficulty in opening the browser. Please copy and open this link: " + e.getURL().toString(),
						"Failed to open link in browser",
						JOptionPane.WARNING_MESSAGE,
						this.icon
				);
			} catch (URISyntaxException ex) {
				// Ignored.
			}
		});

		return textArea;
	}

	private static BufferedImage rescaled(BufferedImage im, double factor) {
		var xform = AffineTransform.getScaleInstance(factor, factor);
		return new AffineTransformOp(xform, AffineTransformOp.TYPE_NEAREST_NEIGHBOR).filter(im, null);
	}
}
