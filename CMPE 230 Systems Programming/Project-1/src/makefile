all:	Main.class CompilerSyntaxException.class
	jar cfm mylang2ir Manifest.txt Main.class CompilerSyntaxException.class
	chmod +x mylang2ir	

Main.class: Main.java
	javac -cp . Main.java
