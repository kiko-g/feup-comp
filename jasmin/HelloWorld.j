.class public HelloWorld
.super java/lang/Object

	.method public <init>()V
		aload_0
		invokenonvirtual java/lang/Object/<init>()V
		return
	.end method

	.method public HelloWorld()V
		.limit stack 99
		.limit locals 99
		aload_0
		invokespecial HelloWorld.<init>()V

		return
	.end method

	.method public static main([Ljava/lang/String;)V
		.limit stack 99
		.limit locals 99
		aload_2
		invokestatic ioPlus.printHelloWorld()V
		return
	.end method

