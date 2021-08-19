/*
 * 	Copyright (C) 2007, CodeGlide - Entwickler, S.A.
 *	All rights reserved.
 *
 *	You may not distribute this software, in whole or in part, without
 *	the express consent of the author.
 *
 *	There is no warranty or other guarantee of fitness of this software
 *	for any purpose.  It is provided solely "as is".
 *
 */
package com.codeglide.util;

import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

import javax.imageio.ImageIO;

public class ImageUtil {
	public static InputStream resampleImage( InputStream in, int maxWidth, int maxHeight ) throws IOException {
		BufferedImage bi = ImageIO.read( in );
		if( bi.getWidth() > maxWidth || bi.getHeight() > maxHeight ) {
			int newWidth = bi.getWidth(), newHeight = bi.getHeight();
			if( newWidth > maxWidth ) {
				newHeight = Math.round((newHeight*maxWidth)/newWidth);
				newWidth = maxWidth;
			}
			if( newHeight > maxHeight ) {
				newWidth = Math.round((newWidth*maxHeight)/newHeight);
				newHeight = maxHeight;
			}
			BufferedImage newBi = new BufferedImage( newWidth, newHeight, bi.getType() );
			newBi.getGraphics().drawImage(bi, 0, 0, newWidth, newHeight, null);
			bi = newBi;
		}
		ByteArrayOutputStream bout = new ByteArrayOutputStream();
		ImageIO.write(bi, "png", bout);
		return new ByteArrayInputStream( bout.toByteArray() );
	}

}
