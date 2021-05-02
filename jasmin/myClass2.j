.class public myClass2
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
		invokespecial myClass2.<init>()V
		return
	.end method

	.method public sum([I[I)[I
		.limit stack 99
		.limit locals 99
		aload_1
		arraylength
		istore_3
		iload_3
		newarray int
		astore 4
		iconst_0
		istore 6
	Loop:
		aload_1
		arraylength
		istore_3
		iload 6
		iload_3
		if_icmplt End
		aload_1
		istore 7
		aload_2
		istore 8
		iload 7
		iload 8
		iadd
		istore 9
		iload 9
		astore 4
		iload 6
		iconst_1
		iadd
		istore 6
		goto Loop
	End:
		aload 4
		areturn
	.end method

