import java.util.*;

public class ArrayDimensionMapper {
    public static void main(String[] args) {
        Scanner sc = new Scanner(System.in);

        // Step 1: Get linear array input
        System.out.println("Enter the numbers (space-separated): ");
        String[] input = sc.nextLine().trim().split(" ");
        int[] numbers = Arrays.stream(input).mapToInt(Integer::parseInt).toArray();

        // Step 2: Ask for dimension (only up to 3)
        System.out.print("Enter number of dimensions (1 to 3): ");
        int dim = sc.nextInt();

        if (dim < 1 || dim > 3) {
            System.out.println("Invalid dimension. Only 1D to 3D supported.");
            return;
        }

        // Step 3: Ask for shape
        int rows = 1, cols = 1, pages = 1;
        if (dim == 1) {
            System.out.print("Enter number of rows: ");
            rows = sc.nextInt();
        } else if (dim == 2) {
            System.out.print("Enter number of rows: ");
            rows = sc.nextInt();
            System.out.print("Enter number of columns: ");
            cols = sc.nextInt();
        } else {
            System.out.print("Enter number of rows: ");
            rows = sc.nextInt();
            System.out.print("Enter number of columns: ");
            cols = sc.nextInt();
            System.out.print("Enter number of pages: ");
            pages = sc.nextInt();
        }

        int totalRequired = rows * cols * pages;
        if (totalRequired != numbers.length) {
            System.out.println("By your numbers, you can't make this array.");
            return;
        }

        // Step 4: Ask layout choice
        System.out.print("Enter layout (1 for Row-Major, 2 for Column-Major): ");
        int layout = sc.nextInt();

        System.out.println("\nGenerated Array:\n");

        if (dim == 1) {
            for (int i = 0; i < numbers.length; i++) {
                System.out.print(numbers[i] + " ");
            }
        }

        else if (dim == 2) {
            int[][] arr = new int[rows][cols];
            if (layout == 1) { // Row-Major
                int index = 0;
                for (int i = 0; i < rows; i++) {
                    for (int j = 0; j < cols; j++) {
                        arr[i][j] = numbers[index++];
                    }
                }
            } else { // Column-Major
                int index = 0;
                for (int j = 0; j < cols; j++) {
                    for (int i = 0; i < rows; i++) {
                        arr[i][j] = numbers[index++];
                    }
                }
            }

            for (int[] row : arr) {
                System.out.println(Arrays.toString(row));
            }
        }

        else if (dim == 3) {
            int[][][] arr = new int[pages][rows][cols];
            if (layout == 1) { // Row-Major
                int index = 0;
                for (int p = 0; p < pages; p++) {
                    for (int i = 0; i < rows; i++) {
                        for (int j = 0; j < cols; j++) {
                            arr[p][i][j] = numbers[index++];
                        }
                    }
                }
            } else { // Column-Major
                int index = 0;
                for (int p = 0; p < pages; p++) {
                    for (int j = 0; j < cols; j++) {
                        for (int i = 0; i < rows; i++) {
                            arr[p][i][j] = numbers[index++];
                        }
                    }
                }
            }

            for (int p = 0; p < pages; p++) {
                System.out.println("Page " + (p + 1) + ":");
                for (int i = 0; i < rows; i++) {
                    System.out.println(Arrays.toString(arr[p][i]));
                }
                System.out.println();
            }
        }

        sc.close();
    }
}
