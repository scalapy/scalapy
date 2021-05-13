from run import benchmarks, runs, configurations

import numpy as np

bench_and_size = []
for (bench, sizes, _) in benchmarks:
    for size in sizes:
        bench_and_size.append(bench + "-" + str(size))

def config_data(bench, conf):
    out = []
    for run in range(runs):
        try:
            points = []
            with open('bench/results/{}/{}/{}'.format(conf, bench, run)) as data:
                for line in data.readlines():
                    points.append(float(line))
            # take only last 2000 to account for startup
            if len(points) < 100:
                points = points[-10:]
            else:
                points = points[-2000:]
            # filter out 1% worst measurements as outliers
            pmax = np.percentile(points, 99)
            for point in points:
                if point <= pmax:
                    out.append(point)
        except IOError:
            pass
    return np.array(out)

def peak_performance():
    out = []
    for bench, sizes, _ in benchmarks:
        for size in sizes:
            res = []
            for conf in configurations:
                try:
                    processed = config_data(bench + "-" + str(size), conf)
                    print("{} ({}) - {}: mean {} ns, stddev {} ns".format(bench, size, conf, np.percentile(processed, 50), np.std(processed)))
                    res.append(np.percentile(processed, 50))
                except IndexError:
                    res.append(0)
            out.append([bench, str(size)] + [str(x) for x in res])
    return out

if __name__ == '__main__':
    leading = ['name', "size"]
    for conf in configurations:
        leading.append(conf)
    zipped_means = peak_performance()
    print(','.join(leading))
    for res in zipped_means:
        print(','.join(res))

