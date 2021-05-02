.class public myClass1
.super java/lang/Object

	.method public <init>()V
		aload_0
		invokenonvirtual java/lang/Object/<init>()V
		return
	.end method

	.method public myClass()V
		.limit stack 99
		.limit locals 99
		aload_0
		invokespecial myClass1.<init>()V
		return
	.end method

	.method public sum([I)I
		.limit stack 99
		.limit locals 99
		iconst_0
		istore_2
		iconst_0
		istore_3
	Loop:
		aload_1
		arraylength
		istore 4
		iload_3
		iload 4
		if_icmplt End
		aload_1
		istore 5
		iload_2
		iload 5
		iadd
		istore_2
		iload_3
		iconst_1
		iadd
		istore_3
		goto Loop
	End:
		iload_2
		ireturn
	.end method

