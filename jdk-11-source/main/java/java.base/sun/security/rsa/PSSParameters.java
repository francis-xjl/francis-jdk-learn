/*
 * Copyright (c) 2018, Oracle and/or its affiliates. All rights reserved.
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

package sun.security.rsa;

import java.io.*;
import sun.security.util.*;
import sun.security.x509.*;
import java.security.AlgorithmParametersSpi;
import java.security.NoSuchAlgorithmException;
import java.security.spec.AlgorithmParameterSpec;
import java.security.spec.InvalidParameterSpecException;
import java.security.spec.MGF1ParameterSpec;
import java.security.spec.PSSParameterSpec;
import static java.security.spec.PSSParameterSpec.DEFAULT;

/**
 * This class implements the PSS parameters used with the RSA
 * signatures in PSS padding. Here is its ASN.1 definition:
 * RSASSA-PSS-params ::= SEQUENCE {
 *   hashAlgorithm      [0] HashAlgorithm     DEFAULT sha1,
 *   maskGenAlgorithm   [1] MaskGenAlgorithm  DEFAULT mgf1SHA1,
 *   saltLength         [2] INTEGER           DEFAULT 20
 *   trailerField       [3] TrailerField      DEFAULT trailerFieldBC
 * }
 *
 * @author Valerie Peng
 *
 */

public final class PSSParameters extends AlgorithmParametersSpi {

    private String mdName;
    private MGF1ParameterSpec mgfSpec;
    private int saltLength;
    private int trailerField;

    private static final ObjectIdentifier OID_MGF1 =
           ObjectIdentifier.newInternal(new int[] {1,2,840,113549,1,1,8});

    public PSSParameters() {
    }

    @Override
    protected void engineInit(AlgorithmParameterSpec paramSpec)
            throws InvalidParameterSpecException {
        if (!(paramSpec instanceof PSSParameterSpec)) {
            throw new InvalidParameterSpecException
                ("Inappropriate parameter specification");
        }
        PSSParameterSpec spec = (PSSParameterSpec) paramSpec;
        this.mdName = spec.getDigestAlgorithm();
        String mgfName = spec.getMGFAlgorithm();
        if (!mgfName.equalsIgnoreCase("MGF1")) {
            throw new InvalidParameterSpecException("Unsupported mgf " +
                mgfName + "; MGF1 only");
        }
        AlgorithmParameterSpec mgfSpec = spec.getMGFParameters();
        if (!(mgfSpec instanceof MGF1ParameterSpec)) {
            throw new InvalidParameterSpecException("Inappropriate mgf " +
                "parameters; non-null MGF1ParameterSpec only");
        }
        this.mgfSpec = (MGF1ParameterSpec) mgfSpec;
        this.saltLength = spec.getSaltLength();
        this.trailerField = spec.getTrailerField();
    }

    @Override
    protected void engineInit(byte[] encoded) throws IOException {
        // first initialize with the DEFAULT values before
        // retrieving from the encoding bytes
        this.mdName = DEFAULT.getDigestAlgorithm();
        this.mgfSpec = (MGF1ParameterSpec) DEFAULT.getMGFParameters();
        this.saltLength = DEFAULT.getSaltLength();
        this.trailerField = DEFAULT.getTrailerField();

        DerInputStream der = new DerInputStream(encoded);
        DerValue[] datum = der.getSequence(4);
        for (DerValue d : datum) {
            if (d.isContextSpecific((byte) 0x00)) {
                // hash algid
                this.mdName = AlgorithmId.parse
                    (d.data.getDerValue()).getName();
            } else if (d.isContextSpecific((byte) 0x01)) {
                // mgf algid
                AlgorithmId val = AlgorithmId.parse(d.data.getDerValue());
                if (!val.getOID().equals(OID_MGF1)) {
                    throw new IOException("Only MGF1 mgf is supported");
                }
                AlgorithmId params = AlgorithmId.parse(
                    new DerValue(val.getEncodedParams()));
                String mgfDigestName = params.getName();
                switch (mgfDigestName) {
                case "SHA-1":
                    this.mgfSpec = MGF1ParameterSpec.SHA1;
                    break;
                case "SHA-224":
                    this.mgfSpec = MGF1ParameterSpec.SHA224;
                    break;
                case "SHA-256":
                    this.mgfSpec = MGF1ParameterSpec.SHA256;
                    break;
                case "SHA-384":
                    this.mgfSpec = MGF1ParameterSpec.SHA384;
                    break;
                case "SHA-512":
                    this.mgfSpec = MGF1ParameterSpec.SHA512;
                    break;
                case "SHA-512/224":
                    this.mgfSpec = MGF1ParameterSpec.SHA512_224;
                    break;
                case "SHA-512/256":
                    this.mgfSpec = MGF1ParameterSpec.SHA512_256;
                    break;
                default:
                    throw new IOException
                        ("Unrecognized message digest algorithm " +
                        mgfDigestName);
                }
            } else if (d.isContextSpecific((byte) 0x02)) {
                // salt length
                this.saltLength = d.data.getDerValue().getInteger();
                if (this.saltLength < 0) {
                    throw new IOException("Negative value for saltLength");
                }
            } else if (d.isContextSpecific((byte) 0x03)) {
                // trailer field
                this.trailerField = d.data.getDerValue().getInteger();
                if (this.trailerField != 1) {
                    throw new IOException("Unsupported trailerField value " +
                    this.trailerField);
                }
            } else {
                throw new IOException("Invalid encoded PSSParameters");
            }
        }
    }

    @Override
    protected void engineInit(byte[] encoded, String decodingMethod)
            throws IOException {
        if ((decodingMethod != null) &&
            (!decodingMethod.equalsIgnoreCase("ASN.1"))) {
            throw new IllegalArgumentException("Only support ASN.1 format");
        }
        engineInit(encoded);
    }

    @Override
    protected <T extends AlgorithmParameterSpec>
            T engineGetParameterSpec(Class<T> paramSpec)
            throws InvalidParameterSpecException {
        if (PSSParameterSpec.class.isAssignableFrom(paramSpec)) {
            return paramSpec.cast(
                new PSSParameterSpec(mdName, "MGF1", mgfSpec,
                                     saltLength, trailerField));
        } else {
            throw new InvalidParameterSpecException
                ("Inappropriate parameter specification");
        }
    }

    @Override
    protected byte[] engineGetEncoded() throws IOException {
        DerOutputStream tmp = new DerOutputStream();
        DerOutputStream tmp2, tmp3;

        // MD
        AlgorithmId mdAlgId;
        try {
            mdAlgId = AlgorithmId.get(mdName);
        } catch (NoSuchAlgorithmException nsae) {
            throw new IOException("AlgorithmId " + mdName +
                                  " impl not found");
        }
        tmp2 = new DerOutputStream();
        mdAlgId.derEncode(tmp2);
        tmp.write(DerValue.createTag(DerValue.TAG_CONTEXT, true, (byte)0),
                      tmp2);

        // MGF
        tmp2 = new DerOutputStream();
        tmp2.putOID(OID_MGF1);
        AlgorithmId mgfDigestId;
        try {
            mgfDigestId = AlgorithmId.get(mgfSpec.getDigestAlgorithm());
        } catch (NoSuchAlgorithmException nase) {
            throw new IOException("AlgorithmId " +
                    mgfSpec.getDigestAlgorithm() + " impl not found");
        }
        mgfDigestId.encode(tmp2);
        tmp3 = new DerOutputStream();
        tmp3.write(DerValue.tag_Sequence, tmp2);
        tmp.write(DerValue.createTag(DerValue.TAG_CONTEXT, true, (byte)1),
                  tmp3);

        // SaltLength
        tmp2 = new DerOutputStream();
        tmp2.putInteger(saltLength);
        tmp.write(DerValue.createTag(DerValue.TAG_CONTEXT, true, (byte)2),
                  tmp2);

        // TrailerField
        tmp2 = new DerOutputStream();
        tmp2.putInteger(trailerField);
        tmp.write(DerValue.createTag(DerValue.TAG_CONTEXT, true, (byte)3),
                  tmp2);

        // Put all together under a SEQUENCE tag
        DerOutputStream out = new DerOutputStream();
        out.write(DerValue.tag_Sequence, tmp);
        return out.toByteArray();
    }

    @Override
    protected byte[] engineGetEncoded(String encMethod) throws IOException {
        if ((encMethod != null) &&
            (!encMethod.equalsIgnoreCase("ASN.1"))) {
            throw new IllegalArgumentException("Only support ASN.1 format");
        }
        return engineGetEncoded();
    }

    @Override
    protected String engineToString() {
        StringBuilder sb = new StringBuilder();
        sb.append("MD: " + mdName + "\n")
            .append("MGF: MGF1" + mgfSpec.getDigestAlgorithm() + "\n")
            .append("SaltLength: " + saltLength + "\n")
            .append("TrailerField: " + trailerField + "\n");
        return sb.toString();
    }
}
