package miniSQL;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class IndexManager 
{
	final static int ORDER = 4;
	final static int LEFT = ORDER / 2 + 1;
	final static int RIGHT = (ORDER+1) / 2;
	
	public static void main(String[] args)
	{
		BTree<Integer> testTree = new BTree<Integer>();
//		int[] list = {0, 14, 54, 20, 35, 25, 63, 55, 44, 33, 76, 53, 8, 38, 42, 82, 84, 87, 78, 99, 31, 3, 74, 24, 65, 79, 39, 12, 75, 83, 80, 96, 92, 95, 32, 34, 89, 52, 19, 94, 11, 21, 60, 28, 57, 2, 30, 41, 70, 22, 43, 7, 64, 9, 77,85, 66, 36, 88, 17, 4, 37, 90, 93, 61, 15, 69, 86, 50, 47, 51, 49, 5, 73,56, 81, 45, 58, 97, 48, 71, 13, 1, 10, 62, 16, 68, 27, 91, 67, 40, 72, 18, 46, 26, 98, 6, 29, 23, 59};
//		int[] list = {1, 2, 3, 5, 4, 19, 14, 13, 18, 10, 6, 7, 8, 9, 12, 11, 17, 16, 15};
		int[] list = {245, 642, 280, 439, 189, 933, 191, 344, 908, 627, 633, 179, 419, 425, 499, 794, 310, 519, 133, 652, 162, 423, 74, 168, 980, 912, 273, 864, 6, 361, 617, 557, 769, 73, 376, 598, 454, 904, 82, 565, 349, 104, 820, 489, 836, 137, 297, 117, 184, 368, 966, 90, 378, 804, 251, 21, 365, 838, 747, 699, 644, 30, 468, 2, 697, 807, 965, 177, 342, 336, 944, 441, 515, 949, 707, 775, 57, 688, 496, 294, 220, 257, 782, 314, 926, 869, 176, 772, 420, 596, 14, 750, 931, 25, 226, 180, 855, 12, 834, 927, 724, 322, 677, 49, 77, 114, 65, 68, 115, 779, 232, 479, 295, 111, 891, 672, 676, 197, 995, 768, 198, 925, 987, 708, 793, 561, 950, 602, 164, 127, 160, 246, 362, 19, 53, 745, 669, 710, 171, 72, 370, 407, 853, 692, 175, 857, 592, 271, 845, 326, 353, 249, 948, 166, 416, 843, 222, 518, 762, 621, 261, 272, 223, 533, 433, 118, 571, 735, 286, 8, 394, 543, 377, 580, 283, 351, 898, 986, 26, 603, 961, 734, 985, 823, 792, 213, 570, 352, 972, 902, 470, 675, 438, 814, 483, 142, 145, 573, 743, 488, 67, 122, 186, 514, 587, 649, 773, 918, 209, 559, 639, 897, 651, 153, 453, 776, 563, 462, 66, 516, 616, 803, 884, 371, 752, 321, 298, 637, 634, 500, 76, 771, 802, 502, 388, 881, 847, 658, 187, 541, 863, 901, 859, 770, 554, 984, 630, 761, 385, 921, 207, 895, 308, 60, 172, 990, 505, 40, 567, 667, 544, 709, 909, 619, 967, 575, 367, 242, 345, 806, 100, 102, 405, 17, 757, 870, 978, 662, 826, 269, 27, 465, 725, 458, 201, 610, 392, 259, 393, 939, 192, 876, 994, 962, 348, 406, 917, 613, 590, 300, 38, 447, 954, 578, 759, 141, 185, 905, 485, 886, 79, 605, 887, 553, 258, 229, 534, 851, 624, 956, 449, 528, 856, 935, 178, 704, 50, 928, 512, 957, 466, 428, 417, 800, 892, 683, 415, 940, 431, 330, 829, 531, 629, 70, 480, 23, 46, 635, 228, 656, 195, 138, 347, 91, 655, 159, 381, 379, 996, 293, 340, 815, 576, 594, 448, 686, 906, 841, 797, 504, 235, 476, 270, 720, 16, 244, 445, 15, 366, 464, 313, 530, 787, 846, 723, 693, 301, 196, 583, 781, 866, 536, 819, 149, 105, 275, 96, 333, 945, 307, 788, 150, 508, 983, 215, 460, 702, 976, 809, 821, 147, 604, 39, 260, 517, 620, 106, 650, 391, 430, 165, 558, 81, 825, 618, 410, 766, 758, 234, 874, 42, 641, 32, 267, 3, 623, 169, 861, 550, 521, 506, 739, 615, 812, 719, 764, 535, 463, 751, 98, 498, 157, 920, 9, 923, 636, 181, 0, 989, 614, 791, 134, 695, 58, 452, 831, 332, 183, 231, 718, 125, 28, 991, 850, 894, 818, 324, 706, 154, 99, 281, 660, 92, 777, 936, 569, 247, 789, 883, 862, 292, 701, 89, 218, 527, 398, 929, 748, 190, 254, 668, 236, 934, 691, 705, 756, 801, 754, 716, 546, 830, 80, 459, 922, 822, 632, 640, 139, 88, 684, 854, 124, 893, 289, 796, 915, 253, 973, 938, 968, 593, 255, 126, 507, 900, 816, 591, 682, 252, 412, 626, 959, 584, 749, 318, 315, 212, 206, 760, 240, 600, 487, 540, 107, 486, 442, 765, 456, 638, 5, 666, 356, 755, 946, 849, 372, 607, 320, 10, 665, 581, 225, 303, 795, 896, 380, 574, 384, 311, 276, 783, 64, 233, 278, 444, 158, 875, 455, 45, 601, 360, 284, 562, 120, 547, 878, 363, 473, 731, 872, 309, 128, 205, 998, 714, 982, 97, 357, 868, 585, 539, 609, 999, 526, 537, 740, 663, 997, 475, 495, 914, 48, 317, 848, 364, 556, 942, 312, 238, 689, 94, 203, 713, 325, 112, 728, 22, 54, 910, 440, 674, 123, 572, 730, 941, 285, 977, 671, 390, 287, 492, 582, 44, 970, 109, 305, 429, 732, 374, 469, 208, 152, 722, 726, 409, 680, 579, 645, 780, 422, 646, 916, 434, 778, 41, 833, 737, 753, 979, 625, 131, 63, 700, 136, 715, 522, 907, 457, 426, 913, 871, 827, 805, 472, 679, 343, 304, 256, 532, 329, 116, 953, 711, 888, 698, 696, 555, 200, 403, 87, 199, 103, 930, 93, 890, 612, 432, 421, 188, 62, 837, 840, 369, 211, 24, 328, 331, 31, 628, 451, 146, 648, 274, 494, 678, 727, 144, 216, 681, 4, 61, 51, 589, 69, 736, 108, 443, 622, 951, 903, 733, 969, 877, 817, 262, 1, 647, 101, 529, 670, 389, 411, 204, 174, 86, 484, 237, 193, 858, 844, 400, 335, 511, 239, 75, 919, 18, 129, 659, 548, 78, 401, 396, 491, 341, 988, 302, 202, 413, 151, 551, 832, 418, 268, 474, 712, 95, 435, 383, 250, 879, 11, 155, 119, 673, 33, 471, 161, 395, 566, 824, 595, 263, 799, 167, 56, 523, 354, 509, 744, 217, 992, 130, 437, 482, 387, 981, 327, 375, 742, 538, 952, 110, 911, 958, 350, 7, 586, 37, 402, 786, 542, 282, 880, 334, 937, 265, 382, 767, 227, 943, 299, 643, 13, 323, 59, 121, 611, 657, 55, 414, 338, 219, 835, 399, 163, 932, 520, 481, 386, 685, 450, 810, 264, 974, 729, 839, 564, 524, 182, 741, 690, 606, 306, 288, 661, 358, 493, 140, 337, 29, 654, 43, 882, 20, 397, 477, 885, 631, 221, 763, 867, 784, 170, 811, 497, 525, 852, 135, 446, 545, 339, 774, 608, 427, 224, 214, 687, 924, 560, 993, 738, 279, 478, 248, 503, 964, 316, 84, 467, 798, 963, 143, 194, 971, 808, 319, 373, 490, 243, 408, 230, 210, 785, 552, 461, 47, 148, 790, 568, 746, 501, 277, 975, 597, 955, 291, 132, 664, 52, 873, 599, 71, 960, 173, 577, 899, 35, 510, 694, 588, 404, 424, 355, 653, 703, 156, 889, 296, 83, 721, 842, 359, 513, 241, 717, 865, 947, 436, 813, 266, 290, 860, 346, 36, 113, 828, 549, 34, 85};
//		int[] list = {0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16, 17, 18, 19, 20, 21, 22, 23, 24, 25, 26, 27, 28, 29, 30, 31, 32, 33, 34, 35, 36, 37, 38, 39, 40, 41, 42, 43, 44, 45, 46, 47, 48, 49, 50, 51, 52, 53, 54, 55, 56, 57, 58, 59, 60, 61, 62, 63, 64, 65, 66, 67, 68, 69, 70, 71, 72, 73, 74, 75, 76, 77, 78, 79, 80, 81, 82, 83, 84, 85, 86, 87, 88, 89, 90, 91, 92, 93, 94, 95, 96, 97, 98, 99};
		for(int i = 0; i < list.length; i++)
		{
//			System.out.println("Inserting " + list[i]);
			testTree.insert(list[i], list[i]);
//			System.out.println(testTree);
		}
		System.out.println(testTree);
		testTree.delete(-1);
		System.out.println(testTree);
//		for(int i = 0; i < list.length; i++)
//		{
//			System.out.println("delete " + list[i]);
//			testTree.delete(list[i]);
//			System.out.println(testTree);
//		}
		System.out.println(testTree);
		System.out.println(testTree.search(11));
	}
}

class BTree<T> implements Serializable
{
	private static final long serialVersionUID = 2649451328948936129L;
	final static int ORDER = IndexManager.ORDER;
	final static int LEFT = IndexManager.LEFT;
	final static int RIGHT = IndexManager.RIGHT;
	Node<T> root;
	
	BTree()
	{
		root = new LeafNode<T>(ORDER);
	}
	
	public String toString()
	{
		return root.toString();
	}
	
	void reset()
	{
		root = new LeafNode<T>(ORDER);
	}
	
	void insert(T element, int shift)
	{
		Node<T> newNode = insert(root, element, shift);
		if(newNode != null)
		{
			Node<T> newRoot = new Node<T>(ORDER);
			newRoot.childNum = 2;
			newRoot.children.set(0, root);
			newRoot.children.set(1, newNode);
			newRoot.keys.set(0, root.getLeastElement());
			newRoot.keys.set(1, newNode.getLeastElement());
			
			root = newRoot;
		}
	}
	
	Node<T> insert(Node<T> node, T element, int shift)
	{
		int index;
		if(node instanceof LeafNode)  // leaf node
		{
			LeafNode<T> leafNode = ((LeafNode<T>)node);
			index = searchInsertIndex(node.keys, node.childNum, element)+1;
			if(node.childNum == ORDER)  // need to split
			{
				LeafNode<T> returnLeafNode = new LeafNode<T>(ORDER);
				if(index < LEFT) // element will be in the left
				{
					// right:   1) copy key&shifts   2) set childNum
					copyArrayList(leafNode.keys,  LEFT-1, ORDER, returnLeafNode.keys);
					copyArrayList(leafNode.shifts, LEFT-1, ORDER, returnLeafNode.shifts);  // .. 1)
					returnLeafNode.childNum = RIGHT;                                       // .. 2)
					// left:    1) shift keys&shifts       2) put element  3) set childNum 
					shiftArrayList(leafNode.keys, index, LEFT-1, 1);
					shiftArrayList(leafNode.shifts, index, LEFT-1, 1);                     // .. 1)
					leafNode.keys.set(index, element);
					leafNode.shifts.set(index, shift);                                     // .. 2)
					leafNode.childNum = LEFT;                                              // .. 3)
					// return node
					return returnLeafNode;
				}
				else // element will be in the right
				{
					// right:   1) copy key&shift  2) shift keys&shifts  3) put element   4) set childNum
					copyArrayList(leafNode.keys, LEFT, ORDER, returnLeafNode.keys);
					copyArrayList(leafNode.shifts, LEFT, ORDER, returnLeafNode.shifts);   // .. 1)
					shiftArrayList(returnLeafNode.keys, index-LEFT, RIGHT-1, 1);
					shiftArrayList(returnLeafNode.shifts, index-LEFT, RIGHT-1, 1);        // .. 2)
					returnLeafNode.keys.set(index-LEFT, element);
					returnLeafNode.shifts.set(index-LEFT, shift);                         // .. 3)
					returnLeafNode.childNum = RIGHT;                                      // .. 4)
					// left:    1) set childNum
					leafNode.childNum = LEFT;
					// return node
					return returnLeafNode;
				}
			}  // if(node.childNum == ORDER)
			else // do not need to split
			{
				// 1)  shift keys&shifts   2) put element  3) set childNum
				shiftArrayList(leafNode.keys, index, node.childNum, 1);
				shiftArrayList(leafNode.shifts, index, node.childNum, 1);                 // .. 1)
				leafNode.keys.set(index, element);
				leafNode.shifts.set(index, shift);                                        // .. 2)
				leafNode.childNum ++;                                                     // .. 3)
				// do not need return any node
				return null;
			}
		}
		else // non-leaf node
		{
			index = searchInsertIndex(node.keys, node.childNum, element);
			if(index == -1)
			{
				index = 0;   // insert into children[index]
				node.keys.set(0, element);
			}
			Node<T> newNode = insert(node.children.get(index), element, shift);
			if(newNode != null) // there is new node created  // newNode should be in children[index+1]
			{
				if(node.childNum == ORDER) // need to split
				{
					Node<T> returnNode = new Node<T>(ORDER);   // returnNode will be the right node
					if(index+1 < LEFT)  // newNode will be in the left
					{
						// right: 1) copy children&keys   2) set childNum
						copyArrayList(node.children, LEFT-1, ORDER, returnNode.children);
						copyArrayList(node.keys, LEFT-1, ORDER, returnNode.keys);         // .. 1)
						returnNode.childNum = RIGHT;                                      // .. 2)
						// left:  1) shift children&keys  2) put element  3) set childNum
						shiftArrayList(node.children, index+1, LEFT-1, 1);
						shiftArrayList(node.keys, index+1, LEFT-1, 1);                    // .. 1)
						node.children.set(index+1, newNode);
						node.keys.set(index+1, newNode.getLeastElement());                // .. 2)
						node.childNum = LEFT;                                             // .. 3)
						// return node
						return returnNode;
					}
					else // newNode will be in the right
					{
						// right: 1) copy children&keys  2) shift keys&shifts  3) put element   4) set childNum
						copyArrayList(node.children, LEFT, ORDER, returnNode.children);
						copyArrayList(node.keys, LEFT, ORDER, returnNode.keys);           // .. 1)
						shiftArrayList(returnNode.children, index+1-LEFT, RIGHT-1, 1);
						shiftArrayList(returnNode.keys, index+1-LEFT, RIGHT-1, 1);              // .. 2)
						returnNode.children.set(index+1-LEFT, newNode);
						returnNode.keys.set(index+1-LEFT, newNode.getLeastElement());     // .. 3)
						returnNode.childNum = RIGHT;                                      // .. 4)
						// left:  1) set childNum
						node.childNum = LEFT;
						// return node
						return returnNode;
					}
				}
				else  // do not need split // insert newNode into node.children[index]
				{
					// 1) shift children&keys  2) put element  3) set childNum 4) return null
					shiftArrayList(node.children, index+1, node.childNum, 1);
					shiftArrayList(node.keys, index+1, node.childNum, 1);                 // .. 1)
					node.children.set(index+1, newNode);
					node.keys.set(index+1, newNode.getLeastElement());                    // .. 2)
					node.childNum ++;                                                     // .. 3)
					return null;                                                          // .. 4)
				}
			}
		}
		return null;
	}
	
	int search(T element)
	{
		return search(root, element);
	}
	
	int search(Node<T> node, T element)
	{
		if(node instanceof LeafNode)
		{
			int index = searchDeleteIndex(node.keys, node.childNum, element);
			if(index == -1)
				return -1;
			else
				return (int)(((LeafNode<T>)node).shifts.get(index));
		}
		else
		{
			int index = searchInsertIndex(node.keys, node.childNum, element);
			return search(node.children.get(index), element);
		}
	}
	
	void delete(T element)
	{
		Node<T> node = delete(root, element);
		if(node != null)
		{
			if(node.childNum == 1 && !(node instanceof LeafNode))
			{
				root = node.children.get(0);
			}
		}
	}
	
	Node<T> delete(Node<T> node, T element)
	{
		int index;
		if(node instanceof LeafNode)
		{
			LeafNode<T> leafNode = (LeafNode<T>)node;
			index = searchDeleteIndex(leafNode.keys, leafNode.childNum, element);
			if(index != -1) // element found
			{
				// 1) shift keys&shifts 2) set childNum  3) return 
				shiftArrayList(leafNode.keys, index+1, leafNode.childNum, -1);
				shiftArrayList(leafNode.shifts, index+1, leafNode.childNum, -1); // ... 1)
				leafNode.childNum --;                                            // ... 2)
				if(leafNode.childNum < RIGHT)
					return leafNode;                                             // ... 3)
			}
			// else element not found // exit
		}
		else // non-leaf node
		{
			index = searchInsertIndex(node.keys, node.childNum, element);
			if(index == -1) // element not found // exit
				return null;
			Node<T> returnedNode = delete(node.children.get(index), element);
			node.keys.set(index, node.children.get(index).getLeastElement());
			if(returnedNode != null) // returnedNode has less than RIGHT children
			{
				if(returnedNode instanceof LeafNode) // returnedNode is LeafNode
				{
					LeafNode<T> returnedLeaf = (LeafNode<T>)returnedNode;
					if(index > 0)  // returnedNode can merge with the left node
						// or can borrow one element from left node
					{
						LeafNode<T> left = (LeafNode<T>)(node.children.get(index-1));
						if(left.childNum <= LEFT)  // merge with the left node
						{
							// 1) merge keys&shifts 2) update node's info 3) set null 4) set childNum 5) return
							mergeChild(left, returnedLeaf);                           // ... 1)
							shiftArrayList(node.children, index+1, node.childNum, -1);
							shiftArrayList(node.keys, index+1, node.childNum, -1);    // ... 2)
							node.children.set(node.childNum-1, null);                 // ... 3)
							node.childNum --;                                         // ... 4)
							if(node.childNum < RIGHT)
								return node;                                          // ... 5)
						}
						else // returnedNode can borrow one element from left node
						{
							// right:  1) shift keys&shifts 2) set keys[0]&shifts[0] 3) set childNum
							shiftArrayList(returnedLeaf.keys, 0, returnedLeaf.childNum, 1);
							shiftArrayList(returnedLeaf.shifts, 0, returnedLeaf.childNum, 1);  // ... 1)
							returnedLeaf.keys.set(0, left.keys.get(left.childNum-1));
							returnedLeaf.shifts.set(0, left.shifts.get(left.childNum-1));      // ... 2)
							returnedLeaf.childNum ++;                                          // ... 3)
							// left:  1) set childNum
							left.childNum --;                                                  // ... 1)
							// node:  1) update keys
							node.keys.set(index, returnedLeaf.getLeastElement());              // ... 1)
						}
					}
					else if(index < node.childNum-1) // returnedNode can merge with the right node
						// or can borrow one element from right node
					{
						LeafNode<T> right = (LeafNode<T>)(node.children.get(index+1));
						if(right.childNum <= LEFT) // returnedLeaf can merge with the right leaf
						{
							// 1) merge keys&shifts 2) update node's info 3) set null 4) set childNum 5) return
							mergeChild(returnedLeaf, right);                           // ... 1)
							shiftArrayList(node.children, index+2, node.childNum, -1);
							shiftArrayList(node.keys, index+2, node.childNum, -1);   // ... 2)
							node.children.set(node.childNum-1, null);                  // ... 3)
							node.childNum --;                                          // ... 4)
							if(node.childNum < RIGHT)
								return node;                                           // ... 5)
						}
						else // returnedLeaf can borrow one element from right node
						{
							// left:   1) append one element 2) set childNum
							returnedLeaf.keys.set(returnedNode.childNum, right.keys.get(0));
							returnedLeaf.shifts.set(returnedNode.childNum, right.shifts.get(0));// ... 1)
							returnedLeaf.childNum ++;                                           // ... 2)
							// right:  1) shift keys&shifts 2) set childNum
							shiftArrayList(right.keys, 1, right.childNum, -1);
							shiftArrayList(right.shifts, 1, right.childNum, -1);                // ... 1)
							right.childNum --;                                                  // ... 2)
							// node:   1) update keys
							node.keys.set(index+1, right.getLeastElement());                    // ... 1)
						}
					}
					else // node has just one child
					{
						return returnedNode;
					}
				}  // if(returnedNode instanceof LeafNode)
				else // returnedNode is not a LeafNode
				{
					if(index > 0) // returnedNode can merge with the left node
						// or can borrow one element from left node
					{
						Node<T> left = node.children.get(index-1);
						if(left.childNum <= LEFT) // merge with the left node
						{
							// 1) merge keys&children 2) update node's info 3) set null 4) set childNum 5) return
							mergeChild(left, returnedNode);
							shiftArrayList(node.children, index+1, node.childNum, -1);
							shiftArrayList(node.keys, index+1, node.childNum, -1);
							node.children.set(node.childNum-1, null);
							node.childNum --;
							if(node.childNum < RIGHT)
								return node;
						}
						else // returnedNode can borrow one child from left node
						{
							// right:  1) shift keys&children 2) set keys[0]&children[0] 3) set childNum
							shiftArrayList(returnedNode.keys, 0, returnedNode.childNum, 1);
							shiftArrayList(returnedNode.children, 0, returnedNode.childNum, 1);// ... 1)
							returnedNode.keys.set(0, left.keys.get(left.childNum-1));
							returnedNode.children.set(0, left.children.get(left.childNum-1));  // ... 2)
							returnedNode.childNum ++;                                          // ... 3)
							// left:  1) set childNum
							left.childNum --;                                                  // ... 1)
							// node:  1) update keys
							node.keys.set(index, returnedNode.getLeastElement());              // ... 1)
						}
					}
					else if(index < node.childNum-1)  // returnedNode can merge with the right node
						// or can borrow one element from right node
					{
						Node<T> right = node.children.get(index+1);
						if(right.childNum <= LEFT) // returnedNode can merge with the right node
						{
							// 1) merge keys&children 2) update node's info 3) set null 4) set childNum 5) return
							mergeChild(returnedNode, right);                           // ... 1)
							shiftArrayList(node.children, index+2, node.childNum, -1);
							shiftArrayList(node.keys, index+2, node.childNum, -1);   // ... 2)
							node.children.set(node.childNum-1, null);                  // ... 3)
							node.childNum --;                                          // ... 4)
							if(node.childNum < RIGHT)
								return node;                                           // ... 5)
						}
						else // returnedNode can borrow one child from right node
						{
							// left:   1) append one child 2) set childNum
							returnedNode.keys.set(returnedNode.childNum, right.keys.get(0));
							returnedNode.children.set(returnedNode.childNum, right.children.get(0));// ... 1)
							returnedNode.childNum ++;                                           // ... 2)
							// right:  1) shift keys&children 2) set childNum
							shiftArrayList(right.keys, 1, right.childNum, -1);
							shiftArrayList(right.children, 1, right.childNum, -1);                // ... 1)
							right.childNum --;                                                  // ... 2)
							// node:   1) update keys
							node.keys.set(index+1, right.getLeastElement());                    // ... 1)
						}
					}
					else // else node has just one child
					{
						return returnedNode;
					}
				}
			} // if(returnedNode != null)
		}
		return null;
	}
	
	void mergeChild(Node<T> left, Node<T> right) 
	{
		if(left instanceof LeafNode)
		{
			LeafNode<T> leftLeaf = (LeafNode<T>)left;
			LeafNode<T> rightLeaf = (LeafNode<T>)right;
			for(int i = 0; i < right.childNum; i++)
			{
				leftLeaf.shifts.set(i+leftLeaf.childNum, rightLeaf.shifts.get(i));
				leftLeaf.keys.set(i+leftLeaf.childNum, rightLeaf.keys.get(i));
			}
		}
		else
		{
			for(int i = 0; i < right.childNum; i++)
			{
				left.children.set(i+left.childNum, right.children.get(i));
				left.keys.set(i+left.childNum, right.keys.get(i));
			}
		}
		left.childNum += right.childNum;
		right.childNum = 0;
	}

	static<K> int searchDeleteIndex(ArrayList<K> keys, int length, K element)
	{
		for(int index = 0; index < length; index++)
		{
			if(element.equals(keys.get(index)))
				return index;
		}
		return -1;
	}
	
	static<K> int searchInsertIndex(ArrayList<K> keys, int length, K element)
	{
//		if(compare(element, keys.get(0)) < 0)
//			return 0;
		for(int index=0; index < length; index++)
		{
			if(compare(element, keys.get(index)) < 0)
				return index-1;
		}
		return length-1;
	}
	
	static<K> int compare(K left, K right)
	{
		if(left instanceof Integer)
			return (int)left-(int)right;
		else if(left instanceof Character)
			return (char)left-(char)right;
		else if(left instanceof String)
			return ((String)left).compareTo((String)right);
		else
			return 0;
	}
	
	static <K> void copyArrayList(ArrayList<K> source, int startIndex, int endIndex, ArrayList<K> destination) 
	{
		for(int i = startIndex; i < endIndex; i++)
		{
			destination.set(i-startIndex, source.get(i));
		}
	}
	
	static <K> void shiftArrayList(ArrayList<K> list, int startIndex, int endIndex, int shift) 
	// move children[startIndex : endIndex] to children[startIndex+shift : endIndex+shift]
	{
		if(shift > 0)
		{
			for(int i = endIndex-1; i >= startIndex && i >= 0 ; i--)
				list.set(i+shift, list.get(i));
		}
		else if(shift < 0)
		{
			for(int i = startIndex; i < endIndex; i++)
				list.set(i+shift, list.get(i));
		}
	}

	List<Integer> getAllShifts() 
	{
		return getAllShifts(root);
	}
	
	List<Integer> getAllShifts(Node<T> node)
	{
		if(node instanceof LeafNode)
		{
			return ((LeafNode<T>)node).shifts.subList(0, node.childNum);
		}
		else // non-leaf node
		{
			ArrayList<Integer> result = new ArrayList<Integer>();
			for(int i = 0; i < node.childNum; i++)
			{
				result.addAll(getAllShifts(node.children.get(i)));
			}
			return result;
		}
	}

}

class Node<T> implements Serializable
{
	private static final long serialVersionUID = -87083404163260980L;
	int childNum;
	ArrayList<Node<T>> children;
	ArrayList<T> keys;
	
	Node(int order)
	{
		childNum = 0;
		children = new ArrayList<Node<T>>(order);
		keys = new ArrayList<T>(order);
		for(int i = 0; i < order; i++)
		{
			children.add(null);
			keys.add(null);
		}
	}
	
	Node()
	{
		childNum = 0;
		children = null;
	}
	
	T getLeastElement()
	{
		return keys.get(0);
	}
	
	public String toString()
	{
		String result = "Node(" + childNum + "): {";
		for(int i = 0; i < childNum-1; i++)
			result += children.get(i) + ", ";
		result += children.get(childNum-1) + " " + indexesToString() + " }";
		return result;
	}
	
	String indexesToString()
	{
		String result = "Indexes: [";
		for(int i = 0; i < childNum-1; i++)
			result += keys.get(i) + ", ";
		result += keys.get(childNum-1) + "]";
		return result;
	}
}

class LeafNode<T> extends Node<T>
{
	private static final long serialVersionUID = 503134981858520282L;
	ArrayList<Integer> shifts;
	LeafNode<T> next;
	LeafNode(int order)
	{
		super();
		next = null;
		keys = new ArrayList<T>(order);
		shifts = new ArrayList<Integer>(order);
		for(int i = 0; i < order; i++)
		{
			keys.add(null);
			shifts.add(null);
		}
	}
	
	public String toString()
	{
		if(childNum == 0)
			return "emtpy tree";
		String result = "leaf: [";
//		for(int i = 0; i < childNum-1; i++)
//			result += keys.get(i) + "|" + shifts.get(i) + ", ";
//		result = result + keys.get(childNum-1) + "|" + shifts.get(childNum-1) + "]";
		for(int i = 0; i < childNum-1; i++)
			result += keys.get(i) + ", ";
		result += keys.get(childNum-1) + "]";
		return result;
	}
}
