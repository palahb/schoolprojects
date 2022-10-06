from random import randint
import time

def Example(X):
    n = len(X)
    y = 0
    for i in range(n):
        if X[i] == 0:
            for j in range(i,n):
                k = n
                while k >= 1:
                    y = y+1
                    k = k/2
        else:
            for m in range(i,n):
                for t in range(1,n+1):
                    x = n
                    while (x > 0):
                        x = x-t
                        y = y+1
    return y

sizes = [1,10,50,100,200,300,400,500,600,700]

for n in sizes:
    B = [0] * n
    W = [1] * n
    A1 = [1] * n
    A2 = [1] * n
    A3 = [1] * n

    for i in range(n):
        A1[i] = randint(2,4)/3 
        A2[i] = randint(2,4)/3 
        A3[i] = randint(2,4)/3
        # randint(2,4) gives 2 or 3 or 4. When we divide
        # them with 3, we get 0 with a probability of 1/3
        # and 1 with a probability of 2/3.
    t0 = time.clock()
    Example(B)
    t1 = time.clock()
    t_best = t1 - t0

    t0 = time.clock()
    Example(W)
    t1 = time.clock()
    t_worst = t1 - t0

    t0 = time.clock()
    Example(A1)
    t1 = time.clock()
    t_a1 = t1 - t0

    t0 = time.clock()
    Example(A2)
    t1 = time.clock()
    t_a2 = t1 - t0

    t0 = time.clock()
    Example(A3)
    t1 = time.clock()
    t_a3 = t1 - t0

    t_avg = (t_a1 + t_a2 + t_a3)/3.0

    print("Case: Best Size: " + str(n) + " Elapsed Time: " + str(t_best))
    print("Case: Worst Size: " + str(n) + " Elapsed Time: " + str(t_worst))
    print("Case: Average Size: " + str(n) + " Elapsed Time: " + str(t_avg))

