package com.displee.cache;

import com.displee.cache.model.EffectiveVertex;
import com.displee.cache.model.EmissiveTriangle;
import com.displee.cache.model.FaceBillboard;
import lombok.Getter;
import lombok.Setter;
import org.displee.CacheLibrary;
import org.displee.io.impl.InputStream;

/**
 * A class representing the model definition in RS3 format.
 * @author Displee
 */
@Getter
@Setter
public class ModelDefinition {

	private final int id;

	private int anInt1942 = 12;
	private int numVertices = 0;
	private int maxIndex = 0;
	private int numFaces = 0;
	private byte priority = 0;
	private int numTextures = 0;
	private byte[] faceMappings;
	private int anInt1951;
	private int[] verticesX;
	private int[] verticesY;
	private int[] verticesZ;
	private short[] faceIndicesA;
	private short[] faceIndicesB;
	private short[] faceIndicesC;
	private int[] vertexSkins;
	private byte[] faceTypes;
	private byte[] facePriorities;
	private byte[] faceAlphas;
	private int[] faceSkins;
	private short[] faceMaterials;
	private short[] faceColors;
	private short[] faceTextures;
	private short[] textureMappingP;
	private short[] textureMappingM;
	private short[] textureMappingN;
	private int[] textureScaleX;
	private int[] textureScaleY;
	private int[] textureScaleZ;
	private byte[] textureRotation;
	private byte[] textureDirection;
	private int[] textureSpeed;
	private int[] textureTransU;
	private int[] textureTransV;
	private int[] anIntArray1923;
	private byte[] aByteArray1933;
	private byte[] aByteArray1934;
	private byte[] aByteArray1952;
	private float[] aFloatArray1914;
	private float[] aFloatArray1928;
	private EmissiveTriangle[] emitters;
	private EffectiveVertex[] effectors;
	private FaceBillboard[] billboards;

	/**
	 * Constructs a new {@code ModelDefinition} {@code Object}.
	 * @param id The model id.
	 */
	public ModelDefinition(int id) {
		this.id = id;
	}

	/**
	 * Decode this model.
	 * @param library The cache library.
	 */
	public void decode(CacheLibrary library) {
		byte[] modelData = library.getIndex(7).getArchive(id).getFile(0).getData();
		InputStream first = new InputStream(modelData);
		InputStream second = new InputStream(modelData);
		InputStream third = new InputStream(modelData);
		InputStream fourth = new InputStream(modelData);
		InputStream fifth = new InputStream(modelData);
		InputStream sixth = new InputStream(modelData);
		InputStream seventh = new InputStream(modelData);
		int var9 = first.readUnsignedByte();
		if(var9 != 1) {
		} else {
			first.readUnsignedByte();
			this.anInt1942 = first.readUnsignedByte();
			first.setOffset(modelData.length - 26);
			this.numVertices = first.readUnsignedShort();
			this.numFaces = first.readUnsignedShort();
			this.numTextures = first.readUnsignedShort();
			int footerFlags = first.readUnsignedByte();
			boolean hasFaceTypes = (footerFlags & 1) == 1;
			boolean hasParticleEffects = (footerFlags & 2) == 2;
			boolean hasBillboards = (footerFlags & 4) == 4;
			boolean hasExtendedVertexSkins = (footerFlags & 16) == 16;
			boolean hasExtendedTriangleSkins = (footerFlags & 32) == 32;
			boolean hasExtendedBillboards = (footerFlags & 64) == 64;
			boolean var17 = (footerFlags & 128) == 128;
			int modelPriority = first.readUnsignedByte();
			int hasFaceAlpha = first.readUnsignedByte();
			int hasFaceSkins = first.readUnsignedByte();
			int hasFaceTextures = first.readUnsignedByte();
			int hasVertexSkins = first.readUnsignedByte();
			int modelVerticesX = first.readUnsignedShort();
			int modelVerticesY = first.readUnsignedShort();
			int modelVerticesZ = first.readUnsignedShort();
			int faceIndices = first.readUnsignedShort();
			int textureIndices = first.readUnsignedShort();
			int numVertexSkins = first.readUnsignedShort();
			int numFaceSkins = first.readUnsignedShort();
			if(!hasExtendedVertexSkins) {
				if(hasVertexSkins == 1) {
					numVertexSkins = this.numVertices;
				} else {
					numVertexSkins = 0;
				}
			}

			if(!hasExtendedTriangleSkins) {
				if(hasFaceSkins == 1) {
					numFaceSkins = this.numFaces;
				} else {
					numFaceSkins = 0;
				}
			}

			int simpleTextureFaceCount = 0;
			int complexTextureFaceCount = 0;
			int cubeTextureFaceCount = 0;
			int offset;
			if(this.numTextures > 0) {
				this.faceMappings = new byte[this.numTextures];
				first.setOffset(3);

				for(offset = 0; offset < this.numTextures; ++offset) {
					byte type = this.faceMappings[offset] = (byte) first.readByte();
					if(type == 0) {
						++simpleTextureFaceCount;
					}

					if(type >= 1 && type <= 3) {
						++complexTextureFaceCount;
					}

					if(type == 2) {
						++cubeTextureFaceCount;
					}
				}
			}

			offset = 3 + this.numTextures;
			int vertexFlagsOffset = offset;
			offset += this.numVertices;
			int faceTypesOffset = offset;
			if(hasFaceTypes) {
				offset += this.numFaces;
			}

			int facesCompressTypeOffset = offset;
			offset += this.numFaces;
			int facePrioritiesOffset = offset;
			if(modelPriority == 255) {
				offset += this.numFaces;
			}

			int faceSkinsOffset = offset;
			offset += numFaceSkins;
			int vertexSkinsOffset = offset;
			offset += numVertexSkins;
			int faceAlphasOffset = offset;
			if(hasFaceAlpha == 1) {
				offset += this.numFaces;
			}

			int faceIndicesOffset = offset;
			offset += faceIndices;
			int faceMaterialsOffset = offset;
			if(hasFaceTextures == 1) {
				offset += this.numFaces * 2;
			}

			int faceTextureIndicesOffset = offset;
			offset += textureIndices;
			int faceColorsOffset = offset;
			offset += this.numFaces * 2;
			int vertexXOffsetOffset = offset;
			offset += modelVerticesX;
			int vertexYOffsetOffset = offset;
			offset += modelVerticesY;
			int vertexZOffsetOffset = offset;
			offset += modelVerticesZ;
			int simpleTexturesOffset = offset;
			offset += simpleTextureFaceCount * 6;
			int complexTexturesOffset = offset;
			offset += complexTextureFaceCount * 6;
			byte textureBytes = 6;
			if(this.anInt1942 == 14) {
				textureBytes = 7;
			} else if(this.anInt1942 >= 15) {
				textureBytes = 9;
			}

			int texturesScaleOffset = offset;
			offset += complexTextureFaceCount * textureBytes;
			int texturesRotationOffset = offset;
			offset += complexTextureFaceCount;
			int texturesDirectionOffset = offset;
			offset += complexTextureFaceCount;
			int texturesTranslationOffset = offset;
			offset += complexTextureFaceCount + cubeTextureFaceCount * 2;
			int modelDataLength1 = modelData.length;
			int modelDataLength2 = modelData.length;
			int modelDataLength3 = modelData.length;
			int modelDataLength4 = modelData.length;
			int baseY;
			int baseZ;
			if(var17) {
				InputStream var60 = new InputStream(modelData);
				var60.setOffset(modelData.length - 26);
				var60.setOffset(modelData[var60.getOffset() - 1]);
				this.anInt1951 = var60.readUnsignedShort();
				baseY = var60.readUnsignedShort();
				baseZ = var60.readUnsignedShort();
				modelDataLength1 = offset + baseY;
				modelDataLength2 = modelDataLength1 + baseZ;
				modelDataLength3 = modelDataLength2 + this.numVertices;
				modelDataLength4 = modelDataLength3 + this.anInt1951 * 2;
			}

			this.verticesX = new int[this.numVertices];
			this.verticesY = new int[this.numVertices];
			this.verticesZ = new int[this.numVertices];
			this.faceIndicesA = new short[this.numFaces];
			this.faceIndicesB = new short[this.numFaces];
			this.faceIndicesC = new short[this.numFaces];
			if(hasVertexSkins == 1) {
				this.vertexSkins = new int[this.numVertices];
			}

			if(hasFaceTypes) {
				this.faceTypes = new byte[this.numFaces];
			}

			if(modelPriority == 255) {
				this.facePriorities = new byte[this.numFaces];
			} else {
				this.priority = (byte)modelPriority;
			}

			if(hasFaceAlpha == 1) {
				this.faceAlphas = new byte[this.numFaces];
			}

			if(hasFaceSkins == 1) {
				this.faceSkins = new int[this.numFaces];
			}

			if(hasFaceTextures == 1) {
				this.faceMaterials = new short[this.numFaces];
			}

			if(hasFaceTextures == 1 && (this.numTextures > 0 || this.anInt1951 > 0)) {
				this.faceTextures = new short[this.numFaces];
			}

			this.faceColors = new short[this.numFaces];
			if(this.numTextures > 0) {
				this.textureMappingP = new short[this.numTextures];
				this.textureMappingM = new short[this.numTextures];
				this.textureMappingN = new short[this.numTextures];
				if(complexTextureFaceCount > 0) {
					this.textureScaleX = new int[complexTextureFaceCount];
					this.textureScaleY = new int[complexTextureFaceCount];
					this.textureScaleZ = new int[complexTextureFaceCount];
					this.textureRotation = new byte[complexTextureFaceCount];
					this.textureDirection = new byte[complexTextureFaceCount];
					this.textureSpeed = new int[complexTextureFaceCount];
				}

				if(cubeTextureFaceCount > 0) {
					this.textureTransU = new int[cubeTextureFaceCount];
					this.textureTransV = new int[cubeTextureFaceCount];
				}
			}

			first.setOffset(vertexFlagsOffset);
			second.setOffset(vertexXOffsetOffset);
			third.setOffset(vertexYOffsetOffset);
			fourth.setOffset(vertexZOffsetOffset);
			fifth.setOffset(vertexSkinsOffset);
			int baseX = 0;
			baseY = 0;
			baseZ = 0;

			int vertexCount;
			int pflag;
			int xOffset;
			int yOffset;
			int zOffset;
			for(vertexCount = 0; vertexCount < this.numVertices; ++vertexCount) {
				pflag = first.readUnsignedByte();
				xOffset = 0;
				if((pflag & 1) != 0) {
					xOffset = second.readUnsignedSmart();
				}

				yOffset = 0;
				if((pflag & 2) != 0) {
					yOffset = third.readUnsignedSmart();
				}

				zOffset = 0;
				if((pflag & 4) != 0) {
					zOffset = fourth.readUnsignedSmart();
				}

				this.verticesX[vertexCount] = baseX + xOffset;
				this.verticesY[vertexCount] = baseY + yOffset;
				this.verticesZ[vertexCount] = baseZ + zOffset;
				baseX = this.verticesX[vertexCount];
				baseY = this.verticesY[vertexCount];
				baseZ = this.verticesZ[vertexCount];
				if(hasVertexSkins == 1) {
					if(hasExtendedVertexSkins) {
						this.vertexSkins[vertexCount] = fifth.readSmartNS();
					} else {
						this.vertexSkins[vertexCount] = fifth.readUnsignedByte();
						if(this.vertexSkins[vertexCount] == 255) {
							this.vertexSkins[vertexCount] = -1;
						}
					}
				}
			}

			if(this.anInt1951 > 0) {
				first.setOffset(modelDataLength2);
				second.setOffset(modelDataLength3);
				third.setOffset(modelDataLength4);
				this.anIntArray1923 = new int[this.numVertices];
				vertexCount = 0;

				for(pflag = 0; vertexCount < this.numVertices; ++vertexCount) {
					this.anIntArray1923[vertexCount] = pflag;
					pflag += first.readUnsignedByte();
				}

				this.aByteArray1933 = new byte[this.numFaces];
				this.aByteArray1934 = new byte[this.numFaces];
				this.aByteArray1952 = new byte[this.numFaces];
				this.aFloatArray1914 = new float[this.anInt1951];
				this.aFloatArray1928 = new float[this.anInt1951];

				for(vertexCount = 0; vertexCount < this.anInt1951; ++vertexCount) {
					this.aFloatArray1914[vertexCount] = (float)second.readShort() / 4096.0F;
					this.aFloatArray1928[vertexCount] = (float)third.readShort() / 4096.0F;
				}
			}

			first.setOffset(faceColorsOffset);
			second.setOffset(faceTypesOffset);
			third.setOffset(facePrioritiesOffset);
			fourth.setOffset(faceAlphasOffset);
			fifth.setOffset(faceSkinsOffset);
			sixth.setOffset(faceMaterialsOffset);
			seventh.setOffset(faceTextureIndicesOffset);

			for(vertexCount = 0; vertexCount < this.numFaces; ++vertexCount) {
				this.faceColors[vertexCount] = (short)first.readUnsignedShort();
				if(hasFaceTypes) {
					this.faceTypes[vertexCount] = (byte) second.readByte();
				}

				if(modelPriority == 255) {
					this.facePriorities[vertexCount] = (byte) third.readByte();
				}

				if(hasFaceAlpha == 1) {
					this.faceAlphas[vertexCount] = (byte) fourth.readByte();
				}

				if(hasFaceSkins == 1) {
					if(hasExtendedTriangleSkins) {
						this.faceSkins[vertexCount] = fifth.readSmartNS();
					} else {
						this.faceSkins[vertexCount] = fifth.readUnsignedByte();
						if(this.faceSkins[vertexCount] == 255) {
							this.faceSkins[vertexCount] = -1;
						}
					}
				}

				if(hasFaceTextures == 1) {
					this.faceMaterials[vertexCount] = (short)(sixth.readUnsignedShort() - 1);
				}

				if(this.faceTextures != null) {
					if(this.faceMaterials[vertexCount] != -1) {
						if(this.anInt1942 >= 16) {
							this.faceTextures[vertexCount] = (short)(seventh.readSmart() - 1);
						} else {
							this.faceTextures[vertexCount] = (short)(seventh.readUnsignedByte() - 1);
						}
					} else {
						this.faceTextures[vertexCount] = -1;
					}
				}
			}

			this.maxIndex = -1;
			first.setOffset(faceIndicesOffset);
			second.setOffset(facesCompressTypeOffset);
			third.setOffset(modelDataLength1);
			this.decodeIndices(first, second, third);
			first.setOffset(simpleTexturesOffset);
			second.setOffset(complexTexturesOffset);
			third.setOffset(texturesScaleOffset);
			fourth.setOffset(texturesRotationOffset);
			fifth.setOffset(texturesDirectionOffset);
			sixth.setOffset(texturesTranslationOffset);
			this.decodeMapping(first, second, third, fourth, fifth, sixth);
			first.setOffset(offset);
			if(hasParticleEffects) {
				vertexCount = first.readUnsignedByte();
				if(vertexCount > 0) {
					this.emitters = new EmissiveTriangle[vertexCount];

					for(pflag = 0; pflag < vertexCount; ++pflag) {
						xOffset = first.readUnsignedShort();
						yOffset = first.readUnsignedShort();
						byte pri;
						if(modelPriority == 255) {
							pri = this.facePriorities[yOffset];
						} else {
							pri = (byte)modelPriority;
						}

						this.emitters[pflag] = new EmissiveTriangle(xOffset, yOffset, this.faceIndicesA[yOffset], this.faceIndicesB[yOffset], this.faceIndicesC[yOffset], pri);
					}
				}
				pflag = first.readUnsignedByte();
				if(pflag > 0) {
					this.effectors = new EffectiveVertex[pflag];

					for(xOffset = 0; xOffset < pflag; ++xOffset) {
						yOffset = first.readUnsignedShort();
						zOffset = first.readUnsignedShort();
						this.effectors[xOffset] = new EffectiveVertex(yOffset, zOffset);
					}
				}
			}

			if(hasBillboards) {
				vertexCount = first.readUnsignedByte();
				if(vertexCount > 0) {
					this.billboards = new FaceBillboard[vertexCount];
					for(pflag = 0; pflag < vertexCount; ++pflag) {
						xOffset = first.readUnsignedShort();
						yOffset = first.readUnsignedShort();
						if(hasExtendedBillboards) {
							zOffset = first.readSmartNS();
						} else {
							zOffset = first.readUnsignedByte();
							if(zOffset == 255) {
								zOffset = -1;
							}
						}
						byte distance = (byte) first.readByte();
						this.billboards[pflag] = new FaceBillboard(xOffset, yOffset, zOffset, distance);
					}
				}
			}

		}
	}

	/**
	 * Decode the face indices.
	 * @param var1 The first stream.
	 * @param var2 The second stream.
	 * @param var3 The third stream.
	 */
	private void decodeIndices(InputStream var1, InputStream var2, InputStream var3) {
		short var4 = 0;
		short var5 = 0;
		short var6 = 0;
		short var7 = 0;

		for(int var8 = 0; var8 < this.numFaces; ++var8) {
			int var9 = var2.readUnsignedByte();
			int var10 = var9 & 7;
			if(var10 == 1) {
				this.faceIndicesA[var8] = var4 = (short)(var1.readUnsignedSmart() + var7);
				this.faceIndicesB[var8] = var5 = (short)(var1.readUnsignedSmart() + var4);
				this.faceIndicesC[var8] = var6 = (short)(var1.readUnsignedSmart() + var5);
				var7 = var6;
				if(var4 > this.maxIndex) {
					this.maxIndex = var4;
				}

				if(var5 > this.maxIndex) {
					this.maxIndex = var5;
				}

				if(var6 > this.maxIndex) {
					this.maxIndex = var6;
				}
			}

			if(var10 == 2) {
				var5 = var6;
				var6 = (short)(var1.readUnsignedSmart() + var7);
				var7 = var6;
				this.faceIndicesA[var8] = var4;
				this.faceIndicesB[var8] = var5;
				this.faceIndicesC[var8] = var6;
				if(var6 > this.maxIndex) {
					this.maxIndex = var6;
				}
			}

			if(var10 == 3) {
				var4 = var6;
				var6 = (short)(var1.readUnsignedSmart() + var7);
				var7 = var6;
				this.faceIndicesA[var8] = var4;
				this.faceIndicesB[var8] = var5;
				this.faceIndicesC[var8] = var6;
				if(var6 > this.maxIndex) {
					this.maxIndex = var6;
				}
			}

			if(var10 == 4) {
				short var11 = var4;
				var4 = var5;
				var5 = var11;
				var6 = (short)(var1.readUnsignedSmart() + var7);
				var7 = var6;
				this.faceIndicesA[var8] = var4;
				this.faceIndicesB[var8] = var11;
				this.faceIndicesC[var8] = var6;
				if(var6 > this.maxIndex) {
					this.maxIndex = var6;
				}
			}

			if(this.anInt1951 > 0 && (var9 & 8) != 0) {
				this.aByteArray1933[var8] = (byte)var3.readUnsignedByte();
				this.aByteArray1934[var8] = (byte)var3.readUnsignedByte();
				this.aByteArray1952[var8] = (byte)var3.readUnsignedByte();
			}
		}

		++this.maxIndex;
	}

	/**
	 * Decode the texture mappings.
	 * @param var1 The first stream.
	 * @param var2 The second stream.
	 * @param var3 The third stream.
	 * @param var4 The fourth stream.
	 * @param var5 The fifth stream.
	 * @param var6 The sixth stream.
	 */
	private void decodeMapping(InputStream var1, InputStream var2, InputStream var3, InputStream var4, InputStream var5, InputStream var6) {
		for(int var7 = 0; var7 < this.numTextures; ++var7) {
			int var8 = this.faceMappings[var7] & 255;
			if(var8 == 0) {
				this.textureMappingP[var7] = (short)var1.readUnsignedShort();
				this.textureMappingM[var7] = (short)var1.readUnsignedShort();
				this.textureMappingN[var7] = (short)var1.readUnsignedShort();
			}

			if(var8 == 1) {
				this.textureMappingP[var7] = (short)var2.readUnsignedShort();
				this.textureMappingM[var7] = (short)var2.readUnsignedShort();
				this.textureMappingN[var7] = (short)var2.readUnsignedShort();
				if(this.anInt1942 < 15) {
					this.textureScaleX[var7] = var3.readUnsignedShort();
					if(this.anInt1942 < 14) {
						this.textureScaleY[var7] = var3.readUnsignedShort();
					} else {
						this.textureScaleY[var7] = var3.read24BitInt();
					}

					this.textureScaleZ[var7] = var3.readUnsignedShort();
				} else {
					this.textureScaleX[var7] = var3.read24BitInt();
					this.textureScaleY[var7] = var3.read24BitInt();
					this.textureScaleZ[var7] = var3.read24BitInt();
				}

				this.textureRotation[var7] = (byte) var4.readByte();
				this.textureDirection[var7] = (byte) var5.readByte();
				this.textureSpeed[var7] = var6.readByte();
			}

			if(var8 == 2) {
				this.textureMappingP[var7] = (short)var2.readUnsignedShort();
				this.textureMappingM[var7] = (short)var2.readUnsignedShort();
				this.textureMappingN[var7] = (short)var2.readUnsignedShort();
				if(this.anInt1942 < 15) {
					this.textureScaleX[var7] = var3.readUnsignedShort();
					if(this.anInt1942 < 14) {
						this.textureScaleY[var7] = var3.readUnsignedShort();
					} else {
						this.textureScaleY[var7] = var3.read24BitInt();
					}

					this.textureScaleZ[var7] = var3.readUnsignedShort();
				} else {
					this.textureScaleX[var7] = var3.read24BitInt();
					this.textureScaleY[var7] = var3.read24BitInt();
					this.textureScaleZ[var7] = var3.read24BitInt();
				}

				this.textureRotation[var7] = (byte) var4.readByte();
				this.textureDirection[var7] = (byte) var5.readByte();
				this.textureSpeed[var7] = var6.readByte();
				this.textureTransU[var7] = var6.readByte();
				this.textureTransV[var7] = var6.readByte();
			}

			if(var8 == 3) {
				this.textureMappingP[var7] = (short)var2.readUnsignedShort();
				this.textureMappingM[var7] = (short)var2.readUnsignedShort();
				this.textureMappingN[var7] = (short)var2.readUnsignedShort();
				if(this.anInt1942 < 15) {
					this.textureScaleX[var7] = var3.readUnsignedShort();
					if(this.anInt1942 < 14) {
						this.textureScaleY[var7] = var3.readUnsignedShort();
					} else {
						this.textureScaleY[var7] = var3.read24BitInt();
					}

					this.textureScaleZ[var7] = var3.readUnsignedShort();
				} else {
					this.textureScaleX[var7] = var3.read24BitInt();
					this.textureScaleY[var7] = var3.read24BitInt();
					this.textureScaleZ[var7] = var3.read24BitInt();
				}

				this.textureRotation[var7] = (byte) var4.readByte();
				this.textureDirection[var7] = (byte) var5.readByte();
				this.textureSpeed[var7] = var6.readByte();
			}
		}
	}

	@Override
	public String toString() {
		return "Model " + id;
	}

}
