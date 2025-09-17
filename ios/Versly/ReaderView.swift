import SwiftUI

struct ReaderNodeView: View {
    let node: Node

    var body: some View {
        Group {
            if let nodeType = node.data.first?.stringValue {
                switch nodeType {
                // Introductions
                case "iex":
                    ReaderChildNodeView(node: node)
                        .foregroundColor(.secondary)
                        .italic()
                        .padding(.bottom, 16)

                // Titles, Headings, and Labels
                case "s1", "s2", "s3", "ms":
                    ReaderChildNodeView(node: node)
                        .font(.title2)
                        .fontWeight(.bold)
                        .padding(.top, 32)
                        .padding(.bottom, 24)

                case "d":
                    ReaderChildNodeView(node: node)
                        .italic()
                        .padding(.bottom, 24)

                case "sp":
                    if node.data.count > 1, let text = node.data[1].stringValue {
                        Text(text)
                            .font(.title3)
                            .fontWeight(.bold)
                            .italic()
                            .padding(.top, 16)
                            .padding(.bottom, 24)
                    }

                // Paragraphs
                case "p":
                    ReaderChildNodeView(node: node)
                        .padding(.bottom, 16)

                case "m":
                    ReaderChildNodeView(node: node)
                        .padding(.bottom, 16)

                case "pr", "cls":
                    ReaderChildNodeView(node: node)
                        .multilineTextAlignment(.trailing)
                        .padding(.bottom, 16)

                case "pmo", "pmc", "pm":
                    ReaderChildNodeView(node: node)
                        .padding(.leading, 16)
                        .padding(.bottom, 16)

                case "pmr":
                    ReaderChildNodeView(node: node)
                        .padding(.leading, 16)
                        .multilineTextAlignment(.trailing)
                        .padding(.bottom, 16)

                case "pi1":
                    ReaderChildNodeView(node: node)
                        .padding(.leading, 16)
                        .padding(.bottom, 16)

                case "pi2":
                    ReaderChildNodeView(node: node)
                        .padding(.leading, 32)
                        .padding(.bottom, 16)

                case "pi3":
                    ReaderChildNodeView(node: node)
                        .padding(.leading, 48)
                        .padding(.bottom, 16)

                case "mi":
                    ReaderChildNodeView(node: node)
                        .padding(.leading, 16)
                        .padding(.bottom, 16)

                case "nb":
                    ReaderChildNodeView(node: node)

                case "pc":
                    VStack(alignment: .leading) {
                        ReaderChildNodeView(node: node)
                            .padding(.bottom, 16)
                    }
                    .frame(maxWidth: .infinity, alignment: .leading)

                case "b":
                    Spacer()
                        .frame(height: 16)

                // Poetry
                case "q1":
                    ReaderChildNodeView(node: node)
                        .padding(.bottom, 16)

                case "q2":
                    ReaderChildNodeView(node: node)
                        .padding(.leading, 16)
                        .padding(.bottom, 16)

                case "q3":
                    ReaderChildNodeView(node: node)
                        .padding(.leading, 32)
                        .padding(.bottom, 16)

                case "q4":
                    ReaderChildNodeView(node: node)
                        .padding(.leading, 48)
                        .padding(.bottom, 16)

                case "qr":
                    ReaderChildNodeView(node: node)
                        .multilineTextAlignment(.trailing)
                        .padding(.bottom, 16)

                case "qc":
                    ReaderChildNodeView(node: node)
                        .multilineTextAlignment(.center)
                        .padding(.bottom, 16)

                case "qa":
                    ReaderChildNodeView(node: node)
                        .italic()
                        .multilineTextAlignment(.center)
                        .padding(.bottom, 16)

                case "qm1":
                    ReaderChildNodeView(node: node)
                        .padding(.leading, 16)
                        .padding(.bottom, 16)

                case "qm2":
                    ReaderChildNodeView(node: node)
                        .padding(.leading, 32)
                        .padding(.bottom, 16)

                // Lists
                case "li1", "lim":
                    ReaderChildNodeView(node: node)
                        .padding(.leading, 16)
                        .padding(.bottom, 8)

                case "li2":
                    ReaderChildNodeView(node: node)
                        .padding(.leading, 32)
                        .padding(.bottom, 8)

                case "li3":
                    ReaderChildNodeView(node: node)
                        .padding(.leading, 48)
                        .padding(.bottom, 8)

                case "li4":
                    ReaderChildNodeView(node: node)
                        .padding(.leading, 64)
                        .padding(.bottom, 8)

                // Tables
                case "table":
                    ReaderTableView(node: node)

                // Custom nodes
                case "zc":
                    ChapterNodeView(node: node)

                default:
                    Text("Unknown node type: \(nodeType)")
                        .foregroundColor(.red)
                }
            }
        }
    }
}

struct ReaderChildNodeView: View {
    let node: Node

    var body: some View {
        if node.data.count > 1, let spans = node.data[1].arrayValue {
            let attributedText = buildAttributedString(from: spans)
            Text(attributedText)
                .font(.body)
                .lineSpacing(4)
        } else {
            EmptyView()
        }
    }

    private func buildAttributedString(from spans: [Any]) -> AttributedString {
        var result = AttributedString()

        for span in spans {
            if let text = span as? String {
                result += AttributedString(text)
            } else if let spanArray = span as? [Any],
                spanArray.count >= 2,
                let type = spanArray[0] as? String,
                let content = spanArray[1] as? String
            {

                var attributedSpan = AttributedString(content)

                switch type {
                case "v":
                    // Verse number styling
                    attributedSpan.font = .caption
                    attributedSpan.foregroundColor = .secondary
                    attributedSpan.baselineOffset = 4
                    attributedSpan += AttributedString(" ")

                case "qs":
                    attributedSpan.font = .body.italic()

                case "qac":
                    attributedSpan.font = .body.bold().italic()

                case "wj":
                    // Jesus' words in red
                    attributedSpan.foregroundColor = .red

                case "em", "bd":
                    attributedSpan.font = .body.bold()

                case "bk", "qt", "sig", "sls", "tl", "it":
                    attributedSpan.font = .body.italic()

                case "nd", "sc":
                    // Small caps - not directly supported, using bold instead
                    attributedSpan.font = .body.bold()

                case "sup":
                    attributedSpan.font = .caption
                    attributedSpan.baselineOffset = 6

                case "t", "no":
                    // Just add the text as-is
                    break

                default:
                    print("Unknown span type: \(type)")
                }

                result += attributedSpan
            }
        }

        return result
    }
}

struct ReaderTableView: View {
    let node: Node

    var body: some View {
        if node.data.count > 1,
            let tableData = node.data[1].arrayValue,
            let rows = tableData as? [[Any]]
        {

            let maxColumns = rows.map { $0.count }.max() ?? 1

            VStack(alignment: .leading, spacing: 8) {
                ForEach(0..<rows.count, id: \.self) { rowIndex in
                    HStack(spacing: 8) {
                        ForEach(0..<maxColumns, id: \.self) { colIndex in
                            Group {
                                if colIndex < rows[rowIndex].count,
                                    let cellArray = rows[rowIndex][colIndex] as? [Any],
                                    cellArray.count >= 2,
                                    let cellType = cellArray[0] as? String,
                                    let cellContent = cellArray[1] as? String
                                {

                                    Text(cellContent)
                                        .fontWeight(cellType == "th" ? .bold : .regular)
                                        .frame(maxWidth: .infinity, alignment: .leading)
                                        .padding(4)
                                } else {
                                    Text("")
                                        .frame(maxWidth: .infinity, alignment: .leading)
                                        .padding(4)
                                }
                            }
                        }
                    }
                }
            }
            .padding(8)
        } else {
            EmptyView()
        }
    }
}

struct ChapterNodeView: View {
    let node: Node

    var body: some View {
        VStack(spacing: 8) {
            if node.data.count > 1, let bookTitle = node.data[1].stringValue {
                Text(bookTitle)
                    .font(.title3)
                    .fontWeight(.bold)
                    .foregroundColor(.secondary)
            }

            if node.data.count > 2, let chapterNumber = node.data[2].stringValue {
                Text(chapterNumber)
                    .font(.system(size: 72, weight: .bold))
            }
        }
        .frame(maxWidth: .infinity)
        .padding(.top, 40)
        .padding(.bottom, 32)
    }
}
