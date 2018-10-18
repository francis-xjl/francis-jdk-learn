/*
 * Copyright (c) 2011, 2013, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 *
 *
 *
 *
 *
 *
 *
 *
 *
 *
 *
 *
 *
 *
 *
 *
 *
 *
 *
 *
 */

package sun.awt;

import java.awt.*;
import java.awt.geom.*;
import java.awt.image.*;

import sun.java2d.SurfaceData;
import sun.java2d.opengl.CGLLayer;
import sun.lwawt.LWGraphicsConfig;
import sun.lwawt.macosx.CPlatformView;

public abstract class CGraphicsConfig extends GraphicsConfiguration
        implements LWGraphicsConfig {

    private final CGraphicsDevice device;
    private ColorModel colorModel;

    protected CGraphicsConfig(CGraphicsDevice device) {
        this.device = device;
    }

    @Override
    public BufferedImage createCompatibleImage(int width, int height) {
        throw new UnsupportedOperationException("not implemented");
    }

    private static native Rectangle2D nativeGetBounds(int screen);

    @Override
    public Rectangle getBounds() {
        final Rectangle2D nativeBounds = nativeGetBounds(device.getCGDisplayID());
        return nativeBounds.getBounds(); // does integer rounding
    }

    @Override
    public ColorModel getColorModel() {
        if (colorModel == null) {
            colorModel = getColorModel(Transparency.OPAQUE);
        }
        return colorModel;
    }

    @Override
    public ColorModel getColorModel(int transparency) {
        throw new UnsupportedOperationException("not implemented");
    }

    @Override
    public AffineTransform getDefaultTransform() {
        double scaleFactor = device.getScaleFactor();
        return AffineTransform.getScaleInstance(scaleFactor, scaleFactor);
    }

    @Override
    public CGraphicsDevice getDevice() {
        return device;
    }

    @Override
    public AffineTransform getNormalizingTransform() {
        double xscale = device.getXResolution() / 72.0;
        double yscale = device.getYResolution() / 72.0;
        return new AffineTransform(xscale, 0.0, 0.0, yscale, 0.0, 0.0);
    }

    /**
     * Creates a new SurfaceData that will be associated with the given
     * LWWindowPeer.
     */
    public abstract SurfaceData createSurfaceData(CPlatformView pView);

    /**
     * Creates a new SurfaceData that will be associated with the given
     * CGLLayer.
     */
    public abstract SurfaceData createSurfaceData(CGLLayer layer);

    @Override
    public final boolean isTranslucencyCapable() {
        //we know for sure we have capable config :)
        return true;
    }
}
