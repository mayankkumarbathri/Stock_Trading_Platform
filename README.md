# Stock Trading Platform

**CodeAlpha Java Programming Internship — Task 2**

A console simulation of a basic stock market and personal trading portfolio,
built with OOP (separate `Stock`, `Transaction`, `Market`, and `Portfolio`
classes).

## Features
- Simulated market with 5 stocks and live-looking prices.
- Buy and sell shares against a cash balance.
- Portfolio tracks cash, holdings, and total value (with gain/loss vs. the
  starting balance).
- Full transaction history (buy/sell, quantity, price, day).
- "Advance to next day" randomly moves each stock's price by up to ±5%,
  so you can track portfolio performance over time.
- Save/load the portfolio (cash, holdings, current day) to `portfolio.txt`
  so it survives between runs.

## Files
- `StockTradingPlatform.java` — single-file program, no external dependencies.

## Compile & run
```bash
javac StockTradingPlatform.java
java StockTradingPlatform
```
Requires a JDK (17+ recommended). Starts you with $10,000 in simulated cash.

## Menu
```
1. View Market Data
2. Buy Stock
3. Sell Stock
4. View Portfolio
5. View Transaction History
6. Advance to Next Day (simulate market movement)
7. Save Portfolio to File
8. Load Portfolio from File
9. Exit
```

## Suggested GitHub repo name
`CodeAlpha_StockTradingPlatform`

## Note
Written and hand-checked for correct Java syntax; not compiled in the
environment that produced it (no JDK compiler available there). Run the
`javac` step above before relying on it.
