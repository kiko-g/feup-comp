.class public Simple
.super java/lang/Object

	.method public <init>()V
		aload_0
		invokenonvirtual java/lang/Object/<init>()V
		return
	.end method

	.method public Simple()V
		.limit stack 99
		.limit locals 99
		aload_0
		invokespecial Simple.<init>()V

		return
	.end method

	.method public add(II)I
		.limit stack 99
		.limit locals 99
		aload_0
		invokevirtual Simple.constInstr()I
		istore_3
		iload_1
		istore 4
		iload 4
		istore 5
		iload 5
		ireturn
	.end method

	.method public static main([Ljava/lang/String;)V
		.limit stack 99
		.limit locals 99
		bipush 20
		istore_2
		bipush 10
		istore_3
		new Simple
		dup
		astore 4
		aload 4
		invokespecial t543.<init>()V
		aload 4
		astore 6
		aload 6
		invokevirtual s.add(II)I
		istore 7
		iload 7
		istore 8
		aload 9
		invokestatic io.println(I)V
		return
	.end method

	.method public constInstr()I
		.limit stack 99
		.limit locals 99
		iconst_0
		istore_1
		iconst_4
		istore_1
		bipush 8
		istore_1
		bipush 14
		istore_1
		sipush 250
		istore_1
		sipush 400
		istore_1
		sipush 1000
		istore_1
		ldc 100474650
		istore_1
		bipush 10
		istore_1
		iload_1
		ireturn
	.end method

