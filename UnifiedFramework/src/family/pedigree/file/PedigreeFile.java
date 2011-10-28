package family.pedigree.file;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.Hashtable;

import org.apache.commons.lang3.ArrayUtils;

import admixture.parameter.Parameter;

import util.NewIt;
import family.mdr.arsenal.MDRConstant;
import family.pedigree.genotype.BFamilyStruct;
import family.pedigree.genotype.BPerson;

/**
 * @author Guo-Bo Chen, chenguobo@gmail.com
 */
public class PedigreeFile {

	protected char[][] AlleleSet;
	protected short[][] AlleleFreq;
	protected Hashtable<String, BFamilyStruct> familystructure;
	protected ArrayList<String> FamID;

	protected int num_marker;
	protected String titleLine = null;
	protected String pedfile;
	protected boolean header = true;

	// public static String MissingGenotype="00";
	public PedigreeFile() {
		this.familystructure = NewIt.newHashtable();
		FamID = NewIt.newArrayList();
	}

	public String[] getFamListSorted() {
		Enumeration<String> famstrList = this.familystructure.keys();
		String[] FID = new String[familystructure.size()];
		int ind = 0;
		while (famstrList.hasMoreElements()) {
			FID[ind++] = famstrList.nextElement();
		}
//		Arrays.sort(FID);
		return FID;
	}

	public BFamilyStruct getFamilyStruct(String familystrID) {
		return this.familystructure.get(familystrID);
	}

	/**
	 * this method iterates through each family in Hashtable families and adds
	 * up the number of individuals in total across all families
	 * 
	 * @return the total number of individuals in all the family objects in the
	 *         families hashtable
	 */
	public int getNumIndividuals() {
		Enumeration<BFamilyStruct> famEnum = familystructure.elements();
		int total = 0;
		while (famEnum.hasMoreElements()) {
			BFamilyStruct fam = famEnum.nextElement();
			total += fam.getNumPersons();
		}
		return total;
	}

	public Hashtable<String, BFamilyStruct> getFamilyStruct() {
		return familystructure;
	}

	public void initial() throws IOException {

	}

	/**
	 * Taking in a pedigree file in the form of a vector of strings and parses
	 * it. The data parsed is stored in families in the member hashtable
	 * families. Note that the "Linkage" here is the relationship between
	 * relatives in a pedigree, but is not that term of genetics.
	 */
	public void parseLinkage(String infile, int numMarkerInFile, int[] WSNP) throws IOException {
		initial();
		num_marker = WSNP.length;
		AlleleSet = new char[num_marker][2];
		AlleleFreq = new short[num_marker][2];
		for (int i = 0; i < num_marker; i++) {
			AlleleSet[i][0] = AlleleSet[i][1] = Parameter.missing_allele.charAt(0);
		}
		int numMarkers = 0;
		BufferedReader reader = new BufferedReader(new FileReader(new File(infile)));
		pedfile = infile;
		String line;
		BPerson per;
		int k = 0;
		while ((line = reader.readLine()) != null) {

			String[] tokenizer = line.split(MDRConstant.delim);
			int numTokens = tokenizer.length;
			numMarkers = (numTokens - 6) / 2;
			if (numMarkers != numMarkerInFile) {
				throw new IllegalArgumentException("Mismatched Colunm in ped file at line " + (k + 1));
			}

			per = new BPerson(num_marker);

			if (tokenizer.length > 6) {

				per.setFamilyID(tokenizer[0]);
				per.setPersonID(tokenizer[1]);
				per.setDadID(tokenizer[2]);
				per.setMomID(tokenizer[3]);

				try {
					int Gender = Integer.parseInt(tokenizer[4]);
					int Status = Integer.parseInt(tokenizer[5]);

					per.setGender(Gender);
					per.setAffectedStatus(Status);

				} catch (NumberFormatException nfe) {
					throw new IOException("Pedfile error: invalid gender or affected status on line " + (k + 1));
				}

				int c = 0;
				for (int j = 0; j < (tokenizer.length - 6) / 2; j++) {
					int idx = ArrayUtils.indexOf(WSNP, j);
					if(idx < 0) continue;
					try {
						String[] allele = { tokenizer[6 + j * 2], tokenizer[6 + j * 2 + 1] };
						boolean flag = (allele[0].compareTo(Parameter.missing_allele) != 0) && (allele[1].compareTo(Parameter.missing_allele) != 0);
						if (flag) {
							int[] code = recode(c, allele);
							per.addMarker(flag, code[0], code[1], c);
							AlleleFreq[c][code[0]]++; AlleleFreq[c][code[1]]++;
						} else {
							per.addMarker(flag, 0, 0, c);
						}
					} catch (NumberFormatException nfe) {
						throw new IllegalArgumentException("Pedigree file input error: invalid genotype on line " + (k + 1));
					}
					c++;
				}
				// check if the family exists already in the Hashtable
				BFamilyStruct famstr = familystructure.get(per.getFamilyID());
				if (famstr == null) {
					// it doesn't exist, so create a new FamilyStruct object
					famstr = new BFamilyStruct(per.getFamilyID());
					familystructure.put(per.getFamilyID(), famstr);
				}

				if (famstr.getPersons().containsKey(per.getPersonID())) {
					throw new IOException("Person " + per.getPersonID() + " in family " + per.getFamilyID() + " appears more than once.");
				}
				famstr.addPerson(per);
			}
			k++;
		}
	}

	protected int[] recode(int idx, String[] allele) {
		int[] code = { -1, -1 };
		char[] ref = AlleleSet[idx];
		if (ref[1] != Parameter.missing_allele.charAt(0)) { 
			// two detected alleles
			for (int i = 0; i < 2; i++) {
				for (int j = 0; j < 2; j++) {
					if (allele[i].charAt(0) == ref[j]) {
						code[i] = j;
						break;
					}
				}
			}
			if (code[0] == -1 || code[1] == -1) {
				System.err.println("more than 2 alleles in the marker column " + (idx + 1));
			}
		} else {
			// less than two detected alleles
			if (allele[0].compareTo(allele[1]) == 0) {
				// 1 when both alleles are same
				if (ref[0] == Parameter.missing_allele.charAt(0)) {
					// zero detected alleles
					ref[0] = allele[0].charAt(0);
					code[0] = code[1] = 0;
				} else {
					// one detected alleles
					if (allele[0].charAt(0) == ref[0]) {
						code[0] = code[1] = 0;
					} else {
						code[0] = code[1] = 1;
						ref[1] = allele[1].charAt(0);
					}
				}
			} else {
				// 2 when both alleles are different
				if (ref[0] == Parameter.missing_allele.charAt(0)) {
					// zero detected alleles
					ref[0] = allele[0].charAt(0);
					ref[1] = allele[1].charAt(0);
					code[0] = 0;
					code[1] = 1;
				} else {
					// one detected alleles
					if (ref[0] == allele[0].charAt(0)) {
						ref[1] = allele[1].charAt(0);
						code[0] = 0;
						code[1] = 1;
					} else if (ref[0] == allele[1].charAt(0)) {
						ref[1] = allele[0].charAt(0);
						code[0] = 1;
						code[1] = 0;
					} else {
						System.out.println("more than 3 alleles in marker column " + (idx + 1));
					}
				}
			}
		}
		return code;
	}

	public char[][] getPolymorphism() {
		return AlleleSet;
	}

	public short[][] getAlleleFrequency() {
		return AlleleFreq;
	}

	public int getNumMarker() {
		return num_marker;
	}
	
	public void setHeader(boolean flag) {
		header = flag;
	}
	
	public void cleanup() {
		for(int i = 0; i < AlleleSet.length; i++) {
			AlleleSet[i] = null;
			AlleleFreq[i] = null;
		}
		AlleleSet = null;
		AlleleFreq = null;
	}
}