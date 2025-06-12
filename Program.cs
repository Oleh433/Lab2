namespace Lab2
{
    internal class Program
    {
        static int[] array;
        static int globalMin = int.MaxValue;
        static int globalMinIndex = -1;
        static object lockObj = new object();
        static CountdownEvent countdown;

        static void Main()
        {
            int arraySize = 10_000_000;
            int threadCount = 10;

            array = GenerateArray(arraySize);
            countdown = new CountdownEvent(threadCount);

            int chunkSize = arraySize / threadCount;

            for (int i = 0; i < threadCount; i++)
            {
                int start = i * chunkSize;
                int end = (i == threadCount - 1) ? arraySize : start + chunkSize;

                Thread thread = new Thread(() => FindLocalMin(start, end));
                thread.Start();
            }

            countdown.Wait();

            Console.WriteLine($"\nГлобальний мінімум: {globalMin}");
            Console.WriteLine($"Індекс мінімального елемента: {globalMinIndex}");
        }

        static int[] GenerateArray(int size)
        {
            Random rnd = new Random();
            int[] arr = new int[size];

            for (int i = 0; i < size; i++)
                arr[i] = rnd.Next(1, 1_000_000);

            int negativeIndex = rnd.Next(size);
            arr[negativeIndex] = -rnd.Next(1, 1000);

            Console.WriteLine($"Від’ємний елемент вставлено на позицію {negativeIndex}: {arr[negativeIndex]}");

            return arr;
        }

        static void FindLocalMin(int start, int end)
        {
            int localMin = int.MaxValue;
            int localMinIndex = -1;

            for (int i = start; i < end; i++)
            {
                if (array[i] < localMin)
                {
                    localMin = array[i];
                    localMinIndex = i;
                }
            }

            lock (lockObj)
            {
                if (localMin < globalMin)
                {
                    globalMin = localMin;
                    globalMinIndex = localMinIndex;
                }
            }

            countdown.Signal();
        }
    }
}
