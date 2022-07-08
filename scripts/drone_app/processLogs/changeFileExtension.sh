for f in *.log; do 
    mv -- "$f" "${f%.log}.txt"
done

for f in *.txt; do
    awk '{gsub(/;/, ",")} 1' "$f" > "${f%.txt}.csv"
done

for f in *.txt; do
    rm $f
done

