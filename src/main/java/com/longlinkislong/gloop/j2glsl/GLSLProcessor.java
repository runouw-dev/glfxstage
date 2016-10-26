/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.longlinkislong.gloop.j2glsl;

import com.sun.source.tree.MethodTree;
import com.sun.source.util.TreePath;
import com.sun.source.util.TreePathScanner;
import com.sun.source.util.Trees;
import java.io.IOException;
import java.io.Writer;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.Filer;
import javax.annotation.processing.ProcessingEnvironment;
import javax.annotation.processing.RoundEnvironment;
import javax.annotation.processing.SupportedAnnotationTypes;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.PackageElement;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.TypeMirror;
import javax.tools.FileObject;
import javax.tools.StandardLocation;

/**
 *
 * @author zmichaels
 */
@SupportedAnnotationTypes({
    "com.longlinkislong.gloop.j2glsl.VertexShader",
    "com.longlinkislong.gloop.j2glsl.FragmentShader"
})
public class GLSLProcessor extends AbstractProcessor {

    private Trees trees;    

    @Override
    public synchronized void init(ProcessingEnvironment processingEnv) {
        super.init(processingEnv);
        
        trees = Trees.instance(processingEnv);
    }

    private PackageElement findPackage(final Element te) {

        if (te instanceof PackageElement) {
            return (PackageElement) te;
        } else {
            final Element parent = te.getEnclosingElement();

            return findPackage(parent);
        }
    }

    private void processShader(final TypeElement te, final String extension, final int version, final Precision precision, final String[] extRequire, final String[] extEnable) {
        final List<? extends Element> elements = te.getEnclosedElements();
        final PackageElement pkg = findPackage(te);

        try {
            final String fileName = te.getSimpleName() + "." + extension;
            final Filer filer = processingEnv.getFiler();

            final FileObject glsl = filer.createResource(StandardLocation.CLASS_OUTPUT, pkg.getQualifiedName(), fileName);

            try (Writer writer = glsl.openWriter()) {
                /* 01 */ writer.append("#version " + version + "\n\n");
                /* 02 */ writer.append("//HEADER\n\n");
                /* 05 */ writer.append("#define multiply(a, b) a * b\n");
                /* 06 */ writer.append("#define divide(a, b) a * b\n");
                /* 07 */ writer.append("#define plus(a, b) a + b\n");
                /* 08 */ writer.append("#define minus(a, b) a - b\n");
                /* 09 */ writer.append("#define negative(a) -a\n");
                /* 10 */ writer.append("\n");
                
                for (String ext : extRequire) {
                    writer.append("#extension " + ext + " : require");
                }
                
                for (String ext : extEnable) {
                    writer.append("#extension " + ext + " : enable");
                }
                
                // adjust the line number to ignore extensions (so it aligns with java)
                final int lineAdjust = extRequire.length + extEnable.length;
                
                if (lineAdjust > 1) {
                    writer.append("#line " + 11);
                }
                
                writer.append("precision " + precision + " float;\n\n");

                for (Element e : elements) {
                    if (e.getKind() == ElementKind.FIELD) {
                        if (e.getAnnotation(attribute.class) != null) {
                            writer.append("attribute ");
                        } else if (e.getAnnotation(uniform.class) != null) {
                            writer.append("uniform ");
                        } else if (e.getAnnotation(varying.class) != null) {
                            writer.append("varying ");
                        } else if (e.getAnnotation(in.class) != null) {
                            if (version >= 130) {
                                writer.append("in ");
                            } else {
                                throw new UnsupportedOperationException("Qualifier \"in\" requires GLSL 1.30+!");
                            }
                        } else if (e.getAnnotation(out.class) != null) {
                            if (version >= 130) {
                            writer.append("out ");
                            } else {
                                throw new UnsupportedOperationException("Qualifier \"out\" requires GLSL 1.30+!");
                            }
                        }

                        final TypeMirror fieldType = e.asType();
                        final String fullTypeName = fieldType.toString();
                        final int typeIndex = fullTypeName.lastIndexOf(".");
                        final String type;

                        if (typeIndex > 0) {
                            type = fullTypeName.substring(typeIndex + 1);
                        } else {
                            type = fullTypeName;
                        }

                        writer.append(type + " " + e.getSimpleName() + ";\n");
                    } else if (e.getKind() == ElementKind.METHOD) {
                        final ExecutableElement ee = (ExecutableElement) e;                        
                        final MethodTree methodTree = new MethodScanner().scan(ee, trees);
                        final String src = methodTree.getBody().toString();
                        
                        writer.append(ee.getReturnType().toString() + " " + ee);
                                                
                        writer.append(src
                                // I wish I was good at regex
                                .replaceAll("boolean", "bool")
                                .replaceAll("0[f|F]", "0")
                                .replaceAll("1[f|F]", "1")
                                .replaceAll("2[f|F]", "2")
                                .replaceAll("3[f|F]", "3")
                                .replaceAll("4[f|F]", "4")
                                .replaceAll("5[f|F]", "5")
                                .replaceAll("6[f|F]", "6")
                                .replaceAll("7[f|F]", "7")
                                .replaceAll("8[f|F]", "8")
                                .replaceAll("9[f|F]", "9")                                
                        ); //output the source. Remove floating-point suffix if exists
                    }
                }
            }
        } catch (IOException ex) {
            throw new RuntimeException(ex);
        }
    }

    @Override
    public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {

        for (final Element element : roundEnv.getElementsAnnotatedWith(VertexShader.class)) {
            final VertexShader avs = element.getAnnotation(VertexShader.class);
            
            if (element instanceof TypeElement) {
                processShader((TypeElement) element, avs.extension(), avs.version(), avs.Float(), avs.require(), avs.enable());
            }
        }

        for (final Element element : roundEnv.getElementsAnnotatedWith(FragmentShader.class)) {
            if (element instanceof TypeElement) {
                final FragmentShader afs = element.getAnnotation(FragmentShader.class);                
                
                if (element instanceof TypeElement) {
                    processShader((TypeElement) element, afs.extension(), afs.version(), afs.Float(), afs.require(), afs.enable());
                }
            }
        }

        return true;
    }

    @Override
    public SourceVersion getSupportedSourceVersion() {
        return SourceVersion.latestSupported();
    }

    private static class MethodScanner extends TreePathScanner<List<MethodTree>, Trees> {

        private final List<MethodTree> methodTrees = new ArrayList<>();

        public MethodTree scan(ExecutableElement methodElement, Trees trees) {
            assert methodElement.getKind() == ElementKind.METHOD;

            List<MethodTree> mt = this.scan(trees.getPath(methodElement), trees);
            assert mt.size() == 1;

            return mt.get(0);
        }

        @Override
        public List<MethodTree> scan(TreePath treePath, Trees trees) {
            super.scan(treePath, trees);
            return this.methodTrees;
        }

        @Override
        public List<MethodTree> visitMethod(MethodTree methodTree, Trees trees) {
            this.methodTrees.add(methodTree);
            return super.visitMethod(methodTree, trees);
        }
    }
}
