import Foundation

struct Node: Codable, Identifiable {
    let id: String
    let data: [AnyCodable]

    init(id: String, data: [Any]) {
        self.id = id
        self.data = data.map(AnyCodable.init)
    }
}

struct Passage: Codable, Identifiable {
    let id = UUID()
    let translation: String
    let book: String
    let bookTitle: String
    let bookAbbreviation: String
    let chapter: String
    let nodes: [Node]
}

struct BookMetadata: Codable, Identifiable {
    let id: String
    let title: String
    let abbreviation: String
    let chapterCount: Int
}

struct Translation: Codable, Identifiable {
    let id: String
    let title: String
    let version: Int
    let isDownloaded: Bool
}

struct ReadingPlan: Codable {
    let plan: PlanData
}

struct PlanData: Codable {
    let id: Int
    let days: [PlanDay]
}

struct PlanDay: Codable {
    let id: Int
    let date: String
    let readings: [Reading]
}

struct Reading: Codable {
    let id: Int
    let book: String
    let chapter: Int
    let range: [Int]
}

struct AnyCodable: Codable {
    private let value: Any

    init(_ value: Any) {
        self.value = value
    }

    init(from decoder: Decoder) throws {
        let container = try decoder.singleValueContainer()

        if let stringValue = try? container.decode(String.self) {
            self.value = stringValue
        } else if let intValue = try? container.decode(Int.self) {
            self.value = intValue
        } else if let doubleValue = try? container.decode(Double.self) {
            self.value = doubleValue
        } else if let boolValue = try? container.decode(Bool.self) {
            self.value = boolValue
        } else if let arrayValue = try? container.decode([AnyCodable].self) {
            self.value = arrayValue.map(\.wrappedValue)
        } else {
            throw DecodingError.typeMismatch(
                AnyCodable.self,
                DecodingError.Context(
                    codingPath: decoder.codingPath,
                    debugDescription: "Unsupported type"))
        }
    }

    func encode(to encoder: Encoder) throws {
        var container = encoder.singleValueContainer()

        switch value {
        case let string as String:
            try container.encode(string)
        case let int as Int:
            try container.encode(int)
        case let double as Double:
            try container.encode(double)
        case let bool as Bool:
            try container.encode(bool)
        case let array as [Any]:
            try container.encode(array.map(AnyCodable.init))
        default:
            throw EncodingError.invalidValue(
                value,
                EncodingError.Context(
                    codingPath: encoder.codingPath,
                    debugDescription: "Unsupported type"))
        }
    }

    var wrappedValue: Any { value }

    subscript(index: Int) -> AnyCodable? {
        guard let array = value as? [Any], array.indices.contains(index) else { return nil }
        return AnyCodable(array[index])
    }

    var stringValue: String? { value as? String }
    var intValue: Int? { value as? Int }
    var doubleValue: Double? { value as? Double }
    var boolValue: Bool? { value as? Bool }
    var arrayValue: [Any]? { value as? [Any] }
}
