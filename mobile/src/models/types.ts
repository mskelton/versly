export interface PassageId {
  book: string;
  chapter: string;
  translation: string;
}

export interface Node {
  id: string;
  data: any[];
}

export interface Passage {
  translation: string;
  book: string;
  bookTitle: string;
  bookAbbreviation: string;
  chapter: string;
  nodes: Node[];
}

export interface BookMetadata {
  id: string;
  title: string;
  abbreviation: string;
  chapterCount: number;
}

export interface Translation {
  id: string;
  title: string;
  lastUpdated: string;
  isDownloaded: boolean;
}

export interface ReadingPlanEntry {
  date: string;
  passages: PassageId[];
}

export interface AppState {
  currentPassage: PassageId;
  selectedTab: 'read' | 'plans' | 'search';
}

export type NodeType =
  | 'p' | 'm' | 'pr' | 'cls' | 'pmo' | 'pmc' | 'pm' | 'pmr'
  | 'pi' | 'pi1' | 'pi2' | 'pi3' | 'mi' | 'nb' | 'pc' | 'b'
  | 'q' | 'q1' | 'q2' | 'q3' | 'q4' | 'qr' | 'qc' | 'qa' | 'qm' | 'qm1' | 'qm2'
  | 'li' | 'li1' | 'li2' | 'li3' | 'li4' | 'lim'
  | 's' | 's1' | 's2' | 's3' | 'ms' | 'd' | 'sp'
  | 'table' | 'tr' | 'th' | 'tc'
  | 'iex'
  | 'v' | 'wj' | 'em' | 'bd' | 'it' | 'bk' | 'qt' | 'sig' | 'sls' | 'tl'
  | 'nd' | 'sc' | 'sup' | 'qs' | 'qac' | 'litl' | 'no' | 't';

export interface USFMNode {
  type: NodeType;
  content?: string;
  children?: USFMNode[];
  attributes?: Record<string, any>;
}
