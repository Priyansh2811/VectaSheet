export function colToLetters(col) {
  let c = col + 1;
  let s = '';
  while (c > 0) {
    const rem = (c - 1) % 26;
    s = String.fromCharCode(65 + rem) + s;
    c = Math.floor((c - 1) / 26);
  }
  return s;
}

export function formatRef(row, col) {
  return `${colToLetters(col)}${row + 1}`;
}
