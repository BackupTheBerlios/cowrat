TOPDIR=
SOURCEDIR=$(TOPDIR)src/
LIBSPATH=
LIBS=$(subst :, ,$(LIBSPATH))
LIBLICENSES= $(TOPDIR)lib/COPYING-libs
DOCS=$(TOPDIR)doc
DATA=$(TOPDIR)sample
MANIFEST=manifest.txt
SOURCES:= $(shell find $(SOURCEDIR) -name *.java)
TARGETS=$(subst .java,.class,$(SOURCES))
JARREDS:=$(subst src/,,$(TARGETS))
JARREDS:=$(subst .class,*.class,$(JARREDS)) Images 
PRJFILES:=`find $(SOURCEDIR) -name prj.el`
DISTDIR:=chronos-`cat VERSION`
DISTFILES=AUTHORS COPYING Makefile README VERSION BUGS INSTALL TODO exclude.txt \
	$(LIBS) $(LIBLICENSES) $(DOCS) $(DATA) \
	$(SOURCES) \
	$(PRJFILES) \
	$(SOURCEDIR)$(MANIFEST) $(SOURCEDIR)Images
#JAVA=jamvm
JAVA=java
JAVAC=javac
#JAVAC=jikes-jsdk
#JAVAC=jikes-classpath
#JAVAC=jikes -bootclasspath /usr/lib/jvm/java-1.5.0-sun/jre/lib/rt.jar -source 1.5
JAR=jar
JARFLAGS= cvmf $(MANIFEST)
JAVAFLAGS=-classpath $(SOURCEDIR):$(LIBSPATH)
JAVACFLAGS=-classpath $(SOURCEDIR):$(LIBSPATH) -encoding ISO-8859-1
JARTARGET=$(TOPDIR)lib/chronos.jar
BINDISTDIR=$(DISTDIR)-bin
BINDISTXTRAS=$(TOPDIR)*COPYING* 

#VPATH=$(SOURCEDIR)

.PHONY: build jar docs dist

all: build jar 

%.class: %.java
	$(JAVAC) $(JAVACFLAGS) $?

build: $(TARGETS)

buildall:
	$(JAVAC) $(JAVAFLAGS) $(SOURCES)

clean:
	find -L . -iregex '.*\(\.aux\|\.log\|\.dvi\|\.class\|~\|#.*#\b\)'  -type f  -print0 |xargs -0 -e rm -f

jar: $(TARGETS)
	cd $(SOURCEDIR) && $(JAR) $(JARFLAGS) ../$(JARTARGET) $(JARREDS)

docs: $(TARGETS)
	javadoc -d $(DOCS) -sourcepath $(SOURCEDIR) -classpath $(SOURCEDIR):$(LIBSPATH) -subpackages ganttchart 

## distribution building, the joy of life...
dist:
	rm -rf $(DISTDIR)
	mkdir $(DISTDIR)
	cp -aL --parents $(DISTFILES) $(DISTDIR)
	tar cfvz /tmp/$(DISTDIR).tar.gz  --exclude-from=exclude.txt $(DISTDIR)
	rm -rf $(DISTDIR)

bindist: jar
	mkdir $(BINDISTDIR)
	cp README.bin $(BINDISTDIR)/README
	cp -aL --parents $(DATA) $(BINDISTDIR)/
	cp -aL --parents $(DOCS) $(BINDISTDIR)/
	cp $(LIBS) $(JARTARGET) $(LIBLICENSES) $(BINDISTDIR)
	tar cfvz /tmp/$(BINDISTDIR).tar.gz  --exclude-from=exclude.txt $(BINDISTDIR)
	rm -rf $(BINDISTDIR)
