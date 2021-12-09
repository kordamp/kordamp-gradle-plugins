package org.kordamp.gradle.plugin.base.extensions;

import org.gradle.api.Project;
import org.gradle.api.plugins.ExtensionAware;
import org.gradle.api.provider.Property;
import org.kordamp.gradle.plugin.base.util.ExtensionPath;
import org.kordamp.gradle.plugin.base.util.ExtensionUtil;

import java.io.File;
import java.util.Comparator;
import java.util.function.Predicate;

public interface ClirrExtension extends ExtensionAware {

    String NAME = "clirr";
    ExtensionPath<ConfigExtension, ClirrExtension> PATH = ConfigExtension.PATH.append(NAME, ClirrExtension.class);

    class Difference implements Comparable<Difference> {
        private String classname;
        private String severity;
        private String identifier;
        private String message;

        public String getClassname() {
            return classname;
        }

        public void setClassname(String classname) {
            this.classname = classname;
        }

        public String getSeverity() {
            return severity;
        }

        public void setSeverity(String severity) {
            this.severity = severity;
        }

        public String getIdentifier() {
            return identifier;
        }

        public void setIdentifier(String identifier) {
            this.identifier = identifier;
        }

        public String getMessage() {
            return message;
        }

        public void setMessage(String message) {
            this.message = message;
        }

        @Override
        public int compareTo(Difference o) {
            return Comparator.nullsFirst(Comparator.comparing(Difference::getClassname)).compare(this, o);
        }
    }

    Property<String> getBaseline();
    Property<File> getFilterFile();
    Property<Predicate<? super Difference>> getFilter();
    Property<Boolean> getFailOnErrors();
    Property<Boolean> getFailOnException();
    Property<Boolean> getSemver();

    static ClirrExtension createIfMissing(Project project) {
        return ExtensionUtil.createIfMissing(project, PATH, (ext, root) -> {
            ext.getBaseline().convention(root.getBaseline());
            ext.getFilterFile().convention(root.getFilterFile());
            ext.getFilter().convention(root.getFilter());
            ext.getFailOnErrors().convention(root.getFailOnErrors().convention(true));
            ext.getFailOnException().convention(root.getFailOnException().convention(false));
            ext.getSemver().convention(root.getSemver().convention(true));
        });
    }

}
