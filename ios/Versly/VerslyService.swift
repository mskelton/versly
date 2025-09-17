import Foundation

class VerslyService: ObservableObject {
    private let baseURL = "https://versly.mskelton.dev/api/"

    func downloadTranslation(_ translation: String) async throws -> Data {
        guard let url = URL(string: "\(baseURL)download/\(translation)") else {
            throw URLError(.badURL)
        }

        let (data, response) = try await URLSession.shared.data(from: url)

        guard let httpResponse = response as? HTTPURLResponse,
            httpResponse.statusCode == 200
        else {
            throw URLError(.badServerResponse)
        }

        return data
    }
}
