.class public myClass3
.super java/lang/Object
	.field private a I

	.method public <init>()V
		aload_0
		invokenonvirtual java/lang/Object/<init>()V
		return
	.end method

	.method public myClass(I)V
		.limit stack 99
		.limit locals 99
		aload_0
		invokespecial myClass3.<init>()V
		iload_2

		putfield myClass3/this I
		return
	.end method

	.method public myClass()V
		.limit stack 99
		.limit locals 99
		aload_0
		invokespecial myClass3.<init>()V
		return
	.end method

	.method public get()I
		.limit stack 99
		.limit locals 99
		iload_2

		getfield myClass3/a I
		istore_1
		iload_1
		ireturn
	.end method

	.method public put(I)V
		.limit stack 99
		.limit locals 99
		iload_2

		putfield myClass3/this I
		return
	.end method

	.method public m1()V
		.limit stack 99
		.limit locals 99
		iload_1

		putfield myClass3/this I
		aload_0
		invokevirtual myClass3.get()I
		istore_2
		new myClass
		dup
		astore_3
		aload_3
		invokespecial c1.<init>()V
		aload_3
		invokevirtual c1.get()I
		istore 5
		aload_3
		invokevirtual c1.put(I)V
		aload_3
		invokevirtual c1.get()I
		istore 6
		return
	.end method

	.method public static main([Ljava/lang/String;)V
		.limit stack 99
		.limit locals 99
		new myClass
		dup
		astore_1
		aload_1
		invokespecial A.<init>()V
		aload_1
		invokevirtual A.m1()V
		return
	.end method

