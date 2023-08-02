-keepattributes SourceFile,LineNumberTable

# Rhino
-keep class javax.script.** { *; }
-keep class com.sun.script.javascript.** { *; }
-keep class org.mozilla.javascript.** { *; }
-keep class com.script.javascript.** { *; }

# 插件相关
-keep class com.github.jing332.tts_server_android.model.rhino.core.** { *; }
-keep class cn.hutool.crypto.** { *; }
-keep class com.hankcs.hanlp.** { *; }

#-keep class cn.hutool.core.** { *; }

-keepnames class * extends java.lang.Exception

# 判断SVG库是否存在 (io.noties.markwon.image.svg.SvgSupport)
-keepnames class com.caverock.androidsvg.SVG


# OKIO
-keep class okio.* { *; }

# 保持 ViewBinding 实现类中的所有名称以 “inflate” 开头的方法不被混淆
-keepclassmembers class * implements androidx.viewbinding.ViewBinding {
    public static ** inflate(...);
}

#-------------- 去掉所有打印 -------------

-assumenosideeffects class android.util.Log {
public static *** d(...);

# public static *** e(...);

public static *** i(...);

public static *** v(...);

public static *** println(...);

public static *** w(...);

public static *** wtf(...);

}

-assumenosideeffects class android.util.Log {
public static *** d(...);

public static *** v(...);

}

-assumenosideeffects class android.util.Log {
# public static *** e(...);

public static *** v(...);

}

-assumenosideeffects class android.util.Log {
public static *** i(...);

public static *** v(...);

}

-assumenosideeffects class android.util.Log {
public static *** w(...);

public static *** v(...);

}

-assumenosideeffects class java.io.PrintStream {
public *** println(...);

public *** print(...);

}

# Keep `Companion` object fields of serializable classes.
# This avoids serializer lookup through `getDeclaredClasses` as done for named companion objects.
-if @kotlinx.serialization.Serializable class **
-keepclassmembers class <1> {
    static <1>$Companion Companion;
}

# Keep `serializer()` on companion objects (both default and named) of serializable classes.
-if @kotlinx.serialization.Serializable class ** {
    static **$* *;
}
-keepclassmembers class <2>$<3> {
    kotlinx.serialization.KSerializer serializer(...);
}

# Keep `INSTANCE.serializer()` of serializable objects.
-if @kotlinx.serialization.Serializable class ** {
    public static ** INSTANCE;
}
-keepclassmembers class <1> {
    public static <1> INSTANCE;
    kotlinx.serialization.KSerializer serializer(...);
}

# @Serializable and @Polymorphic are used at runtime for polymorphic serialization.
-keepattributes RuntimeVisibleAnnotations,AnnotationDefault

# Serializer for classes with named companion objects are retrieved using `getDeclaredClasses`.
# If you have any, uncomment and replace classes with those containing named companion objects.
#-keepattributes InnerClasses # Needed for `getDeclaredClasses`.
#-if @kotlinx.serialization.Serializable class
#com.example.myapplication.HasNamedCompanion, # <-- List serializable classes with named companions.
#com.example.myapplication.HasNamedCompanion2
#{
#    static **$* *;
#}
#-keepnames class <1>$$serializer { # -keepnames suffices; class is kept when serializer() is kept.
#    static <1>$$serializer INSTANCE;
#}

-dontwarn com.bumptech.glide.Glide
-dontwarn com.bumptech.glide.RequestBuilder
-dontwarn com.bumptech.glide.RequestManager
-dontwarn com.bumptech.glide.request.BaseRequestOptions
-dontwarn com.bumptech.glide.request.target.ViewTarget
-dontwarn com.squareup.picasso.Picasso
-dontwarn com.squareup.picasso.RequestCreator
-dontwarn java.awt.AWTException
-dontwarn java.awt.AlphaComposite
-dontwarn java.awt.BasicStroke
-dontwarn java.awt.Color
-dontwarn java.awt.Composite
-dontwarn java.awt.Desktop
-dontwarn java.awt.Dimension
-dontwarn java.awt.Font
-dontwarn java.awt.FontFormatException
-dontwarn java.awt.FontMetrics
-dontwarn java.awt.Graphics2D
-dontwarn java.awt.Graphics
-dontwarn java.awt.GraphicsConfiguration
-dontwarn java.awt.GraphicsDevice
-dontwarn java.awt.GraphicsEnvironment
-dontwarn java.awt.Image
-dontwarn java.awt.Point
-dontwarn java.awt.Rectangle
-dontwarn java.awt.RenderingHints$Key
-dontwarn java.awt.RenderingHints
-dontwarn java.awt.Robot
-dontwarn java.awt.Shape
-dontwarn java.awt.Stroke
-dontwarn java.awt.Toolkit
-dontwarn java.awt.color.ColorSpace
-dontwarn java.awt.datatransfer.Clipboard
-dontwarn java.awt.datatransfer.ClipboardOwner
-dontwarn java.awt.datatransfer.DataFlavor
-dontwarn java.awt.datatransfer.StringSelection
-dontwarn java.awt.datatransfer.Transferable
-dontwarn java.awt.datatransfer.UnsupportedFlavorException
-dontwarn java.awt.font.FontRenderContext
-dontwarn java.awt.geom.AffineTransform
-dontwarn java.awt.geom.Ellipse2D$Double
-dontwarn java.awt.geom.Rectangle2D
-dontwarn java.awt.geom.RoundRectangle2D$Double
-dontwarn java.awt.image.AffineTransformOp
-dontwarn java.awt.image.BufferedImage
-dontwarn java.awt.image.BufferedImageOp
-dontwarn java.awt.image.ColorConvertOp
-dontwarn java.awt.image.ColorModel
-dontwarn java.awt.image.CropImageFilter
-dontwarn java.awt.image.DataBuffer
-dontwarn java.awt.image.DataBufferByte
-dontwarn java.awt.image.DataBufferInt
-dontwarn java.awt.image.FilteredImageSource
-dontwarn java.awt.image.ImageFilter
-dontwarn java.awt.image.ImageObserver
-dontwarn java.awt.image.ImageProducer
-dontwarn java.awt.image.RenderedImage
-dontwarn java.awt.image.SampleModel
-dontwarn java.awt.image.WritableRaster
-dontwarn java.beans.BeanInfo
-dontwarn java.beans.FeatureDescriptor
-dontwarn java.beans.IntrospectionException
-dontwarn java.beans.Introspector
-dontwarn java.beans.PropertyDescriptor
-dontwarn java.beans.PropertyEditor
-dontwarn java.beans.PropertyEditorManager
-dontwarn java.beans.Transient
-dontwarn java.beans.XMLEncoder
-dontwarn java.lang.management.ManagementFactory
-dontwarn java.lang.management.RuntimeMXBean
-dontwarn javax.imageio.IIOImage
-dontwarn javax.imageio.ImageIO
-dontwarn javax.imageio.ImageReader
-dontwarn javax.imageio.ImageTypeSpecifier
-dontwarn javax.imageio.ImageWriteParam
-dontwarn javax.imageio.ImageWriter
-dontwarn javax.imageio.metadata.IIOMetadata
-dontwarn javax.imageio.stream.ImageInputStream
-dontwarn javax.imageio.stream.ImageOutputStream
-dontwarn javax.naming.InitialContext
-dontwarn javax.naming.NamingEnumeration
-dontwarn javax.naming.NamingException
-dontwarn javax.naming.directory.Attribute
-dontwarn javax.naming.directory.Attributes
-dontwarn javax.naming.directory.InitialDirContext
-dontwarn javax.swing.ImageIcon
-dontwarn javax.tools.DiagnosticCollector
-dontwarn javax.tools.DiagnosticListener
-dontwarn javax.tools.FileObject
-dontwarn javax.tools.ForwardingJavaFileManager
-dontwarn javax.tools.JavaCompiler$CompilationTask
-dontwarn javax.tools.JavaCompiler
-dontwarn javax.tools.JavaFileManager$Location
-dontwarn javax.tools.JavaFileManager
-dontwarn javax.tools.JavaFileObject$Kind
-dontwarn javax.tools.JavaFileObject
-dontwarn javax.tools.SimpleJavaFileObject
-dontwarn javax.tools.StandardJavaFileManager
-dontwarn javax.tools.StandardLocation
-dontwarn javax.tools.ToolProvider
-dontwarn javax.xml.bind.JAXBContext
-dontwarn javax.xml.bind.Marshaller
-dontwarn javax.xml.bind.Unmarshaller
-dontwarn org.bouncycastle.asn1.ASN1Encodable
-dontwarn org.bouncycastle.asn1.ASN1InputStream
-dontwarn org.bouncycastle.asn1.ASN1Object
-dontwarn org.bouncycastle.asn1.ASN1ObjectIdentifier
-dontwarn org.bouncycastle.asn1.ASN1Primitive
-dontwarn org.bouncycastle.asn1.ASN1Sequence
-dontwarn org.bouncycastle.asn1.BERSequence
-dontwarn org.bouncycastle.asn1.DERSequence
-dontwarn org.bouncycastle.asn1.DLSequence
-dontwarn org.bouncycastle.asn1.gm.GMNamedCurves
-dontwarn org.bouncycastle.asn1.pkcs.PrivateKeyInfo
-dontwarn org.bouncycastle.asn1.sec.ECPrivateKey
-dontwarn org.bouncycastle.asn1.util.ASN1Dump
-dontwarn org.bouncycastle.asn1.x509.AlgorithmIdentifier
-dontwarn org.bouncycastle.asn1.x509.SubjectPublicKeyInfo
-dontwarn org.bouncycastle.asn1.x9.X9ECParameters
-dontwarn org.bouncycastle.asn1.x9.X9ObjectIdentifiers
-dontwarn org.bouncycastle.cert.X509CertificateHolder
-dontwarn org.bouncycastle.crypto.AlphabetMapper
-dontwarn org.bouncycastle.crypto.BlockCipher
-dontwarn org.bouncycastle.crypto.CipherParameters
-dontwarn org.bouncycastle.crypto.CryptoException
-dontwarn org.bouncycastle.crypto.Digest
-dontwarn org.bouncycastle.crypto.InvalidCipherTextException
-dontwarn org.bouncycastle.crypto.Mac
-dontwarn org.bouncycastle.crypto.digests.SM3Digest
-dontwarn org.bouncycastle.crypto.engines.SM2Engine$Mode
-dontwarn org.bouncycastle.crypto.engines.SM2Engine
-dontwarn org.bouncycastle.crypto.engines.SM4Engine
-dontwarn org.bouncycastle.crypto.macs.CBCBlockCipherMac
-dontwarn org.bouncycastle.crypto.macs.HMac
-dontwarn org.bouncycastle.crypto.params.AsymmetricKeyParameter
-dontwarn org.bouncycastle.crypto.params.ECDomainParameters
-dontwarn org.bouncycastle.crypto.params.ECPrivateKeyParameters
-dontwarn org.bouncycastle.crypto.params.ECPublicKeyParameters
-dontwarn org.bouncycastle.crypto.params.KeyParameter
-dontwarn org.bouncycastle.crypto.params.ParametersWithID
-dontwarn org.bouncycastle.crypto.params.ParametersWithIV
-dontwarn org.bouncycastle.crypto.params.ParametersWithRandom
-dontwarn org.bouncycastle.crypto.signers.DSAEncoding
-dontwarn org.bouncycastle.crypto.signers.PlainDSAEncoding
-dontwarn org.bouncycastle.crypto.signers.SM2Signer
-dontwarn org.bouncycastle.crypto.signers.StandardDSAEncoding
-dontwarn org.bouncycastle.jcajce.provider.asymmetric.ec.BCECPrivateKey
-dontwarn org.bouncycastle.jcajce.provider.asymmetric.ec.BCECPublicKey
-dontwarn org.bouncycastle.jcajce.provider.asymmetric.util.EC5Util
-dontwarn org.bouncycastle.jcajce.provider.asymmetric.util.ECUtil
-dontwarn org.bouncycastle.jcajce.spec.FPEParameterSpec
-dontwarn org.bouncycastle.jcajce.spec.OpenSSHPrivateKeySpec
-dontwarn org.bouncycastle.jcajce.spec.OpenSSHPublicKeySpec
-dontwarn org.bouncycastle.jce.provider.BouncyCastleProvider
-dontwarn org.bouncycastle.jce.spec.ECNamedCurveSpec
-dontwarn org.bouncycastle.jce.spec.ECParameterSpec
-dontwarn org.bouncycastle.jsse.BCSSLParameters
-dontwarn org.bouncycastle.jsse.BCSSLSocket
-dontwarn org.bouncycastle.jsse.provider.BouncyCastleJsseProvider
-dontwarn org.bouncycastle.math.ec.ECCurve
-dontwarn org.bouncycastle.math.ec.ECPoint
-dontwarn org.bouncycastle.math.ec.FixedPointCombMultiplier
-dontwarn org.bouncycastle.openssl.PEMDecryptorProvider
-dontwarn org.bouncycastle.openssl.PEMEncryptedKeyPair
-dontwarn org.bouncycastle.openssl.PEMException
-dontwarn org.bouncycastle.openssl.PEMKeyPair
-dontwarn org.bouncycastle.openssl.PEMParser
-dontwarn org.bouncycastle.openssl.X509TrustedCertificateBlock
-dontwarn org.bouncycastle.openssl.jcajce.JcaPEMKeyConverter
-dontwarn org.bouncycastle.openssl.jcajce.JceOpenSSLPKCS8DecryptorProviderBuilder
-dontwarn org.bouncycastle.openssl.jcajce.JcePEMDecryptorProviderBuilder
-dontwarn org.bouncycastle.operator.InputDecryptorProvider
-dontwarn org.bouncycastle.operator.OperatorCreationException
-dontwarn org.bouncycastle.pkcs.PKCS10CertificationRequest
-dontwarn org.bouncycastle.pkcs.PKCS8EncryptedPrivateKeyInfo
-dontwarn org.bouncycastle.pkcs.PKCSException
-dontwarn org.bouncycastle.util.Arrays
-dontwarn org.bouncycastle.util.BigIntegers
-dontwarn org.bouncycastle.util.encoders.Hex
-dontwarn org.bouncycastle.util.io.pem.PemObject
-dontwarn org.bouncycastle.util.io.pem.PemObjectGenerator
-dontwarn org.bouncycastle.util.io.pem.PemReader
-dontwarn org.bouncycastle.util.io.pem.PemWriter
-dontwarn org.conscrypt.Conscrypt$Version
-dontwarn org.conscrypt.Conscrypt
-dontwarn org.conscrypt.ConscryptHostnameVerifier
-dontwarn org.openjsse.javax.net.ssl.SSLParameters
-dontwarn org.openjsse.javax.net.ssl.SSLSocket
-dontwarn org.openjsse.net.ssl.OpenJSSE
-dontwarn org.commonmark.ext.gfm.strikethrough.Strikethrough
-dontwarn pl.droidsonroids.gif.GifDrawable