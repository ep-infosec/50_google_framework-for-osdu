#   Copyright 2020 Google LLC
#   Licensed under the Apache License, Version 2.0 (the "License");
#   you may not use this file except in compliance with the License.
#   You may obtain a copy of the License at
#
#      https://www.apache.org/licenses/LICENSE-2.0
#
#   Unless required by applicable law or agreed to in writing, software
#   distributed under the License is distributed on an "AS IS" BASIS,
#   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
#   See the License for the specific language governing permissions and
#   limitations under the License.

resource "google_storage_bucket" "osdu_file_bucket" {
  name          = "${var.project}-osdu-file"
  location      = var.region
  storage_class = "STANDARD"

  labels = {
    app = "osdu"
  }
}

resource "google_storage_bucket_iam_member" "osdu_sa" {
  bucket = google_storage_bucket.osdu_file_bucket.name
  role   = "roles/storage.admin"
  member = "serviceAccount:${google_service_account.osdu_service_account.email}"
}
