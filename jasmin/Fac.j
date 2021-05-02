.class public Fac
.super java/lang/Object

	.method public <init>()V
		aload_0
		invokenonvirtual java/lang/Object/<init>()V
		return
	.end method

	.method public Fac()V
		.limit stack 99
		.limit locals 99
		aload_0
		invokespecial Fac.<init>()V
		return
	.end method

	.method public compFac(I)I
		.limit stack 99
		.limit locals 99
		iload_1
		iconst_1
		if_icmplt else
		iconst_1
		istore_2
		goto endif
	else:
		iload_1
		iconst_1
		isub
		istore_3
		aload_0
		invokevirtual Fac.compFac(I)I
		istore 4
		iload_1
		iload 4
		imul
		istore_2
	endif:
		iload_2
		ireturn
	.end method

	.method public static main([Ljava/lang/String;)V
		.limit stack 99
		.limit locals 99
		new Fac
		dup
		astore_1
		aload_1
		invokespecial aux1.<init>()V
		aload_1
		invokevirtual aux1.compFac(I)I
		istore_3
		return
	.end method

